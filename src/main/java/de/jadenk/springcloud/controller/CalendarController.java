package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.CalendarEntry;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.CalendarEntryRepository;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@Controller
@RequestMapping("/calendar") // Basis-URL für Kalenderfunktionen
public class CalendarController {

    @Autowired
    private CalendarEntryRepository entryRepo; // Repository für Kalender-Einträge

    @Autowired
    private UserService userService; // Service, um aktuelle Benutzer zu holen

    /*
     * GET /calendar
     * Zeigt den Kalender für einen bestimmten Monat und Jahr an (Standard: aktueller Monat/Jahr)
     */
    @GetMapping
    public String viewCalendar(@RequestParam(defaultValue = "#{T(java.time.Year).now().value}") int year,
                               @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int month,
                               Model model) {

        User user = getCurrentUser(); // Aktuellen Benutzer abrufen

        YearMonth ym = YearMonth.of(year, month); // gewähltes Jahr/Monat
        List<List<DayWrapper>> weeks = new ArrayList<>();

        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();
        LocalDate cursor = firstDay.with(DayOfWeek.MONDAY); // Kalender beginnt Montag

        // Wochen erstellen, mit DayWrapper für jeden Tag
        while (cursor.isBefore(lastDay.plusDays(7))) {
            List<DayWrapper> week = new ArrayList<>();
            boolean hasValidDay = false;

            for (int i = 0; i < 7; i++) {
                List<CalendarEntry> entries = entryRepo.findVisibleEntriesForDay(cursor, user);
                boolean isToday = cursor.equals(LocalDate.now());
                boolean isCurrentMonth = cursor.getMonth() == ym.getMonth();

                if (isCurrentMonth) {
                    hasValidDay = true;
                }

                week.add(new DayWrapper(cursor, isToday, entries, isCurrentMonth));
                cursor = cursor.plusDays(1);
            }

            if (hasValidDay) {
                weeks.add(week);
            }
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthName", ym.getMonth().toString());
        model.addAttribute("weeks", weeks);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", authorities.stream()
                .findFirst().map(GrantedAuthority::getAuthority).orElse("UNKNOWN"));

        return "calendar"; // Thymeleaf-Template "calendar.html"
    }

    /*
     * POST /calendar/toggle-visibility
     * Sichtbarkeit eines Eintrags zwischen PUBLIC und PRIVATE umschalten
     * Nur Ersteller oder Admins dürfen ändern
     */
    @PostMapping("/toggle-visibility")
    public String toggleVisibility(@RequestParam Long id) {
        Optional<CalendarEntry> entryOpt = entryRepo.findById(id);
        if (entryOpt.isPresent()) {
            CalendarEntry entry = entryOpt.get();
            User currentUser = getCurrentUser();

            boolean isCreator = entry.getUser().equals(currentUser);
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().endsWith("ADMIN"));

            if (isCreator || isAdmin) {
                entry.setVisibility(entry.getVisibility() == CalendarEntry.Visibility.PUBLIC
                        ? CalendarEntry.Visibility.PRIVATE
                        : CalendarEntry.Visibility.PUBLIC);
                entryRepo.save(entry);
            }
        }
        return "redirect:/calendar"; // zurück zum Kalender
    }

    /*
     * GET /calendar/entry/{id}
     * Liefert JSON-Daten für einen einzelnen Eintrag
     * Mit Berechtigung: nur Ersteller oder Admin darf bearbeiten
     */
    @GetMapping("/entry/{id}")
    @ResponseBody
    public ResponseEntity<?> getEntry(@PathVariable Long id) {
        Optional<CalendarEntry> entryOpt = entryRepo.findById(id);
        if (entryOpt.isEmpty()) return ResponseEntity.notFound().build();

        CalendarEntry entry = entryOpt.get();
        User currentUser = getCurrentUser();

        boolean isCreator = entry.getUser().equals(currentUser);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().endsWith("ADMIN"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", entry.getId());
        result.put("title", entry.getTitle());
        result.put("description", entry.getDescription());
        result.put("user", entry.getUser().getUsername());
        result.put("time", entry.getTime());
        result.put("date", entry.getDate());
        result.put("visibility", entry.getVisibility().toString());
        result.put("canEdit", isCreator || isAdmin);

        return ResponseEntity.ok(result);
    }

    /*
     * GET /calendar/entry/view/{id}
     * Liefert das Modal-Fragment für die Anzeige eines Eintrags
     */
    @GetMapping("/entry/view/{id}")
    public String getViewModal(@PathVariable Long id, Model model) {
        Optional<CalendarEntry> entryOpt = entryRepo.findById(id);
        if (entryOpt.isEmpty()) return "fragments/error"; // Fehlerseite, falls Eintrag fehlt

        CalendarEntry entry = entryOpt.get();
        User currentUser = getCurrentUser();

        boolean isCreator = entry.getUser().equals(currentUser);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().endsWith("ADMIN"));

        model.addAttribute("entry", entry);
        model.addAttribute("canEdit", isCreator || isAdmin);

        return "fragments/modal :: modal-content"; // Thymeleaf Fragment
    }

    /*
     * POST /calendar/add
     * Fügt einen neuen Eintrag hinzu
     */
    @PostMapping("/add")
    public String addEntry(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) LocalTime entry_time,
            @RequestParam CalendarEntry.Visibility visibility
    ) {
        User user = getCurrentUser();
        CalendarEntry entry = new CalendarEntry();
        entry.setTitle(title);
        entry.setDate(date);
        entry.setVisibility(visibility);
        entry.setTime(entry_time);
        entry.setUser(user);

        entryRepo.save(entry); // speichern (egal ob PUBLIC oder PRIVATE)

        return "redirect:/calendar?year=" + date.getYear();
    }

    /*
     * GET /calendar/delete/{id}
     * Löscht einen Eintrag
     * Nur der Ersteller oder Admin darf löschen
     */
    @GetMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id) {
        Optional<CalendarEntry> entryOpt = entryRepo.findById(id);
        if (entryOpt.isPresent()) {
            CalendarEntry entry = entryOpt.get();
            if (entry.getUser().equals(getCurrentUser()) || getCurrentUser().getRole().getName().equals("ADMIN")) {
                entryRepo.delete(entry);
            }
        }
        return "redirect:/calendar";
    }

    /*
     * Hilfsmethode, um den aktuell angemeldeten Benutzer abzurufen
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByName(username);
    }

    /*
     * Wrapper-Klasse für einen Tag im Kalender
     * Enthält Datum, Einträge, Kennzeichnung ob heute, ob zum aktuellen Monat gehörend
     */
    public class DayWrapper {
        private LocalDate date;
        private int dayOfMonth;
        private boolean today;
        private boolean isCurrentMonth;
        private List<CalendarEntry> entries;

        public DayWrapper(LocalDate date, boolean isToday, List<CalendarEntry> entries, boolean isCurrentMonth) {
            this.date = date;
            this.dayOfMonth = date.getDayOfMonth();
            this.today = isToday;
            this.entries = entries;
            this.isCurrentMonth = isCurrentMonth;
        }

        public LocalDate getDate() { return date; }
        public int getDayOfMonth() { return dayOfMonth; }
        public boolean isToday() { return today; }
        public List<CalendarEntry> getEntries() { return entries; }
        public boolean isCurrentMonth() { return isCurrentMonth; }
    }
}
