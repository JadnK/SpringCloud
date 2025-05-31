package de.jadenk.springcloud.util;

import de.jadenk.springcloud.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final WebhookService webhookService;

    public ShutdownListener(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("ShutdownListener triggered.");
        webhookService.triggerWebhookEvent(WebhookEvent.SYSTEM_EVENT, "Springcloud Stopped.", null);
    }
}
