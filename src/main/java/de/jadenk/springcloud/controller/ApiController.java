package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.dto.LogDTO;
import de.jadenk.springcloud.dto.UserDTO;
import de.jadenk.springcloud.exception.CustomRuntimeException;
import de.jadenk.springcloud.model.ApiToken;
import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.model.Webhook;
import de.jadenk.springcloud.repository.ApiTokenRepository;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.ApiTokenService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogRepository logRepository;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/logs")
    public List<LogDTO> getAllLogs() {
        return logRepository.findAll().stream()
                .map(LogDTO::new)
                .collect(Collectors.toList());
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/log/{id}")
    public ResponseEntity<LogDTO> getLogById(@PathVariable Long id) {
        return logRepository.findById(id)
                .map(log -> ResponseEntity.ok(new LogDTO(log)))
                .orElse(ResponseEntity.notFound().build());
    }



    // ONLY FOR ADMIN PAGE TO MANAGE Api's
    @Autowired
    private ApiTokenRepository apiTokenRepository;
    @Autowired
    private ApiTokenService apiTokenService;

    @PostMapping("/s/add")
    public String addApi(
            @RequestParam String apiName
    ) throws IOException {
        ApiToken apiToken = new ApiToken();
        String token = UUID.randomUUID().toString().replace("-", "");
        apiToken.setToken(token);
        apiToken.setName(apiName);
        apiToken.setActive(true);

        apiTokenRepository.save(apiToken);

        return "redirect:/admin";
    }

    @PostMapping("/s/delete/{id}")
    public String deleteApiToken(@PathVariable Long id) {
        apiTokenService.deleteApiToken(id);
        return "redirect:/admin";
    }

    @PostMapping("/s/toggle/{id}")
    public String toggleApiToken(@PathVariable Long id, @RequestParam(required = false) Boolean active, RedirectAttributes redirectAttributes) {
        apiTokenRepository.findById(id).ifPresent(apiToken -> {
            apiToken.setActive(active != null && active);
            apiTokenRepository.save(apiToken);
        });
        return "redirect:/admin";
    }

}