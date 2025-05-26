package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.CalendarEntry;
import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.CalendarEntryRepository;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.util.WebhookEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@DependsOn("databaseInitializer")
public class CalendarWebhookScheduler {

    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private WebhookRepository webhookRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(cron = "0 0 0 * * *")
    public void sendDailyCalendarReminders() {

        LocalDate today = LocalDate.now();
        List<CalendarEntry> entries = calendarEntryRepository.findByDate(today);

        if (entries.isEmpty()) return;

        StringBuilder message = new StringBuilder();
        message.append("📅 **Kalendereinträge für heute:**\n\n");

        for (int i = 0; i < entries.size(); i++) {
            CalendarEntry entry = entries.get(i);

            message.append("• **")
                    .append(entry.getTitle()).append("**");

            if (entry.getTime() != null) {
                message.append(" um `").append(entry.getTime().format(TIME_FORMATTER)).append("`");
            }

            message.append(" _(von ").append(entry.getUser().getUsername()).append(")_");

            if (i < entries.size() - 1) {
                message.append("\n");
            }
        }


        webhookService.triggerWebhookEvent(WebhookEvent.CALENDAR_NOTIFICATION, message.toString(), (long)0);
    }
}
