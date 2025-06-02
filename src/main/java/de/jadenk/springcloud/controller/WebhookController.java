package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/webhooks")
public class WebhookController {

    private static final String IMGUR_CLIENT_ID = "SECRET";

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/add")
    public String addWebhook(
            @RequestParam String url,
            @RequestParam String webhookName,
            @RequestParam byte[] webhookPicture,
            @RequestParam(required = false) boolean onUserCreation,
            @RequestParam(required = false) boolean onUserBan,
            @RequestParam(required = false) boolean onUserUpdate,
            @RequestParam(required = false) boolean onRegister,
            @RequestParam(required = false) boolean onErrorThrown,
            @RequestParam(required = false) boolean onFileDeletion,
            @RequestParam(required = false) boolean onFileUpload,
            @RequestParam(required = false) boolean onSystemEvent,
            @RequestParam(required = false) boolean onCalendarNotification
    ) {
        Webhook webhook = new Webhook();
        webhook.setUrl(url);
        webhook.setName(webhookName);
        webhook.setWebhook_image_data(uploadToImgur(webhookPicture));
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


    private String uploadToImgur(byte[] imageBytes) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Client-ID " + IMGUR_CLIENT_ID);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        body.add("image", base64Image);
        body.add("type", "base64");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.imgur.com/3/image", request, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("link");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @PostMapping("/delete/{id}")
    public String deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return "redirect:/admin";
    }

    @PostMapping("/test/{id}")
    public String testWebhook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            webhookService.sendTestPayload(webhook);
        });
        redirectAttributes.addFlashAttribute("message", "Webhook-Test started");
        return "redirect:/admin";
    }


    @PostMapping("/toggle/{id}")
    public String toggleWebhook(@PathVariable Long id, @RequestParam(required = false) Boolean enabled, RedirectAttributes redirectAttributes) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            webhook.setEnabled(enabled != null && enabled);
            webhookRepository.save(webhook);
        });
        redirectAttributes.addFlashAttribute("message", "Webhook aktiviert/deaktiviert");
        return "redirect:/admin";
    }
}
