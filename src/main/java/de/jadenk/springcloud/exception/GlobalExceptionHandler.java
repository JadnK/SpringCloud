package de.jadenk.springcloud.exception;

import de.jadenk.springcloud.service.WebhookService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private WebhookService webhookService;

    @ExceptionHandler(CustomRuntimeException.class)
    public void handleCustomRuntimeException(CustomRuntimeException ex) {
        webhookService.triggerWebhookEvent(WebhookEvent.ERROR_THROWN, ex.getMessage(), 0L);
    }

}
