package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.exception.CustomRuntimeException;
import de.jadenk.springcloud.model.CloudSetting;
import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.service.CloudSettingService;
import de.jadenk.springcloud.service.WebhookService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private CloudSettingService cloudSettingService;

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private WebhookService webhookService;

    // =========================
    // Webhook erstellen
    // =========================
    /**
     * POST /webhooks/add
     * Erstellt einen neuen Webhook mit optionalem Bild und Konfiguration für Events.
     *
     * @param url URL des Webhooks
     * @param webhookName Name des Webhooks
     * @param webhookPicture optionales Bild für den Webhook
     * @param onUserCreation, onUserBan, onUserUpdate, onRegister, onErrorThrown, onFileDeletion, onFileUpload, onSystemEvent, onCalendarNotification
     *        boolean flags für unterschiedliche Trigger-Ereignisse
     */
    @PostMapping("/add")
    public String addWebhook(
            @RequestParam String url,
            @RequestParam String webhookName,
            @RequestParam(required = false) MultipartFile webhookPicture,
            @RequestParam(required = false) boolean onUserCreation,
            @RequestParam(required = false) boolean onUserBan,
            @RequestParam(required = false) boolean onUserUpdate,
            @RequestParam(required = false) boolean onRegister,
            @RequestParam(required = false) boolean onErrorThrown,
            @RequestParam(required = false) boolean onFileDeletion,
            @RequestParam(required = false) boolean onFileUpload,
            @RequestParam(required = false) boolean onSystemEvent,
            @RequestParam(required = false) boolean onCalendarNotification
    ) throws IOException {

        Webhook webhook = new Webhook();
        webhook.setUrl(url);
        webhook.setName(webhookName);

        // Bild optional hochladen (Imgur)
        if (webhookPicture != null && !webhookPicture.isEmpty()) {
            try {
                byte[] imageBytes = webhookPicture.getBytes();
                webhook.setWebhook_image_data(uploadToImgur(imageBytes));
            } catch (CustomRuntimeException e) {
                webhook.setWebhook_image_data(null);
                webhookService.triggerWebhookEvent(WebhookEvent.ERROR_THROWN, e.getMessage(), 0L);
            }
        } else {
            webhook.setWebhook_image_data(null);
        }

        // Event Flags setzen
        webhook.setEnabled(true);
        webhook.setOnUserCreation(onUserCreation);
        webhook.setOnUserBan(onUserBan);
        webhook.setOnUserUpdate(onUserUpdate);
        webhook.setOnRegister(onRegister);
        webhook.setOnErrorThrown(onErrorThrown);
        webhook.setOnFileDeletion(onFileDeletion);
        webhook.setOnFileUpload(onFileUpload);
        webhook.setOnSystemEvent(onSystemEvent);
        webhook.setOnCalendarNotification(onCalendarNotification);

        webhookRepository.save(webhook);

        return "redirect:/admin";
    }

    // =========================
    // Bild hochladen zu Imgur
    // =========================
    /**
     * Uploadet ein Bild zu Imgur und gibt den Link zurück.
     * Benötigt den IMGUR_CLIENT_ID aus den CloudSettings.
     */
    public String uploadToImgur(byte[] imageData) {
        Optional<CloudSetting> clientIdSetting = cloudSettingService.getSetting("IMGUR_CLIENT_ID");

        if (clientIdSetting.isEmpty() || clientIdSetting.get().getValue() == null || clientIdSetting.get().getValue().isEmpty()) {
            throw new CustomRuntimeException("[Webhook Controller] Imgur Client ID not configured.");
        }

        String clientId = "Client-ID " + clientIdSetting.get().getValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", clientId);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        body.add("image", base64Image);
        body.add("type", "base64");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.imgur.com/3/image",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            return (String) data.get("link");
        }

        throw new RuntimeException("Imgur upload failed");
    }

    // =========================
    // Webhook löschen
    // =========================
    @PostMapping("/delete/{id}")
    public String deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return "redirect:/admin";
    }

    // =========================
    // Webhook testen
    // =========================
    @PostMapping("/test/{id}")
    public String testWebhook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            webhookService.sendTestPayload(webhook);
        });
        return "redirect:/admin";
    }

    // =========================
    // Webhook aktivieren/deaktivieren
    // =========================
    @PostMapping("/toggle/{id}")
    public String toggleWebhook(@PathVariable Long id, @RequestParam(required = false) Boolean enabled, RedirectAttributes redirectAttributes) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            webhook.setEnabled(enabled != null && enabled);
            webhookRepository.save(webhook);
        });
        return "redirect:/admin";
    }

}
