package de.jadenk.springcloud.service;

import de.jadenk.springcloud.exception.CustomRuntimeException;
import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders; // <-- DAS ist korrekt!
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.jadenk.springcloud.util.WebhookEvent.USER_CREATION;


@Service
public class WebhookService {
    @Autowired
    private WebhookRepository webhookRepository;

    public Webhook saveWebhook(Webhook webhook) {
        return webhookRepository.save(webhook);
    }

    public List<Webhook> getAll() {
        return webhookRepository.findAll();
    }

    public void deleteWebhook(Long id) {
        webhookRepository.deleteById(id);
    }

    public Optional<Webhook> getFirst() {
        return webhookRepository.findAll().stream().findFirst();
    }

    public void triggerWebhookEvent(WebhookEvent event, String message, Long log_id) {
        List<Webhook> webhooks = webhookRepository.findEnabledWebhooks();

        for (Webhook webhook : webhooks) {
            boolean shouldSend = switch (event) {
                case USER_CREATION -> webhook.isOnUserCreation();
                case USER_BANNED -> webhook.isOnUserBan();
                case USER_UPDATED -> webhook.isOnUserUpdate();
                case USER_REGISTERED -> webhook.isOnRegister();
                case ERROR_THROWN -> webhook.isOnErrorThrown();
                case FILE_DELETED -> webhook.isOnFileDeletion();
                case FILE_UPLOADED -> webhook.isOnFileUpload();
                case CALENDAR_NOTIFICATION -> webhook.isOnCalendarNotification();
            };

            if (shouldSend) {
                sendPayload(webhook.getUrl(), message, log_id);
            }
        }
    }


    public void sendPayload(String url, String message, Long log_id) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();

        StringBuilder contentBuilder = new StringBuilder();

        if (log_id != null && log_id != 0) {
            contentBuilder.append("**Log-ID:** `").append(log_id).append("`\n\n");
        }

        contentBuilder.append("> ").append(message.replace("\n", "\n> "));

        payload.put("content", contentBuilder.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTestPayload(Webhook webhook) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();
        payload.put("content", "✅ Testmessage from SpringCloud-System.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhook.getUrl(), request, String.class);
        } catch (Exception e) {
            throw new CustomRuntimeException("[Webhook Service] Error: " + e);
        }
    }


}
