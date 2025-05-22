package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/add")
    public String addWebhook(
            @RequestParam String url,
            @RequestParam(required = false) boolean onUserCreation,
            @RequestParam(required = false) boolean onUserBan,
            @RequestParam(required = false) boolean onUserUpdate,
            @RequestParam(required = false) boolean onRegister,
            @RequestParam(required = false) boolean onErrorThrown,
            @RequestParam(required = false) boolean onFileDeletion,
            @RequestParam(required = false) boolean onFileUpload,
            @RequestParam(required = false) boolean onCalendarNotification
    ) {
        Webhook webhook = new Webhook();
        webhook.setUrl(url);
        webhook.setEnabled(true);
        webhook.setOnUserCreation(onUserCreation);
        webhook.setOnUserBan(onUserBan);
        webhook.setOnUserUpdate(onUserUpdate);
        webhook.setOnRegister(onRegister);
        webhook.setOnErrorThrown(onErrorThrown);
        webhook.setOnFileDeletion(onFileDeletion);
        webhook.setOnFileUpload(onFileUpload);
        webhook.setOnCalendarNotification(onCalendarNotification);

        webhookRepository.save(webhook);

        return "redirect:/admin";
    }


    @PostMapping("/delete/{id}")
    public String deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return "redirect:/admin";
    }

    @PostMapping("/test/{id}")
    public String testWebhook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            if (webhook.isEnabled()) {
                webhookService.sendTestPayload(webhook);
            }
        });
        redirectAttributes.addFlashAttribute("message", "Webhook-Test ausgeführt");
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
