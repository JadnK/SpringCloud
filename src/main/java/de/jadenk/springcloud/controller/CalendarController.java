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
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private CalendarEntryRepository entryRepo;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCalendar(@RequestParam(defaultValue = "#{T(java.time.Year).now().value}") int year,
                               @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int month, Model model) {

        User user = getCurrentUser();

        YearMonth ym = YearMonth.of(year, month);
        List<List<DayWrapper>> weeks = new ArrayList<>();

        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();
        LocalDate cursor = firstDay.with(DayOfWeek.MONDAY);

        while (cursor.isBefore(lastDay.plusDays(7))) {
            List<DayWrapper> week = new ArrayList<>();
            boolean hasValidDay = false;

            for (int i = 0; i < 7; i++) {
                // Lade nur Einträge dieses Tages, die öffentlich oder vom Benutzer sind
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

        return "calendar";
    }



    @GetMapping("/entry/{id}")
    @ResponseBody
    public ResponseEntity<?> getEntry(@PathVariable Long id) {
        Optional<CalendarEntry> entryOpt = entryRepo.findById(id);
        if (entryOpt.isEmpty()) return ResponseEntity.notFound().build();

        CalendarEntry entry = entryOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("id", entry.getId());
        result.put("title", entry.getTitle());
        result.put("description", entry.getDescription());
        result.put("time", entry.getTime());
        result.put("date", entry.getDate());
        result.put("visibility", entry.getVisibility().toString());

        return ResponseEntity.ok(result);
    }




    @PostMapping("/add")
    public String addEntry(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam CalendarEntry.Visibility visibility
    ) {
        User user = getCurrentUser();
        CalendarEntry entry = new CalendarEntry();
        entry.setTitle(title);
        entry.setDate(date);
        entry.setVisibility(visibility);
        entry.setUser(user);

        if (visibility == CalendarEntry.Visibility.PUBLIC) {
            entryRepo.save(entry);
        } else {
            entryRepo.save(entry);
        }
        return "redirect:/calendar?year=" + date.getYear();
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByName(username);
    }

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

        public LocalDate getDate() {
            return date;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public boolean isToday() {
            return today;
        }

        public List<CalendarEntry> getEntries() {
            return entries;
        }

        public boolean isCurrentMonth() {
            return isCurrentMonth;
        }
    }



}
