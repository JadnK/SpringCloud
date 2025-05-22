package de.jadenk.springcloud.repository;

import de.jadenk.springcloud.model.CalendarEntry;
import de.jadenk.springcloud.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    @Query("SELECT w FROM Webhook w WHERE w.enabled = true")
    List<Webhook> findEnabledWebhooks();
}
