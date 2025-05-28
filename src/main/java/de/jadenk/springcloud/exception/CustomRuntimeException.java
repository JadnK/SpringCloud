package de.jadenk.springcloud.exception;

import de.jadenk.springcloud.service.WebhookService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomRuntimeException extends RuntimeException {

    @Autowired
    private WebhookService webhookService;

    public CustomRuntimeException(String msg) {
        super(msg);
        webhookService.triggerWebhookEvent(WebhookEvent.ERROR_THROWN, msg, 0L);
    }

}
