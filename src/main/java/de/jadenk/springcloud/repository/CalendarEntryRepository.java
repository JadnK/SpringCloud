package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.CalendarEntry;
import de.jadenk.springcloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {
    List<CalendarEntry> findAllByDate(LocalDate date);
    @Query("SELECT e FROM CalendarEntry e WHERE e.date = :date AND (e.user = :user OR e.visibility = 'PUBLIC')")
    List<CalendarEntry> findVisibleEntriesForDay(@Param("date") LocalDate date, @Param("user") User user);
    List<CalendarEntry> findByDateAndUserOrVisibility(LocalDate date, User user, CalendarEntry.Visibility visibility);
    @Query("SELECT e FROM CalendarEntry e JOIN FETCH e.user WHERE e.date = :date")
    List<CalendarEntry> findByDate(@Param("date") LocalDate date);

}
