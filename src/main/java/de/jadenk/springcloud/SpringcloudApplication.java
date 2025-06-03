package de.jadenk.springcloud;

import de.jadenk.springcloud.service.WebhookService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringcloudApplication {

	// curl -k -X GET https://localhost:8080/api/log/2 -H "X-API-TOKEN: feb58cac1cbf427ea9efe12d114cb467" -H "Accept: application/json"

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SpringcloudApplication.class, args);
		WebhookService webhookService = context.getBean(WebhookService.class);
		webhookService.triggerWebhookEvent(WebhookEvent.SYSTEM_EVENT, "SpringCloud started!", 0L);
	}


}
