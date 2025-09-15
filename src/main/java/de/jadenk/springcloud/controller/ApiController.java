package de.jadenk.springcloud.controller;

import de.jadenk.springcloud.dto.LogDTO;
import de.jadenk.springcloud.dto.UploadedFileDTO;
import de.jadenk.springcloud.dto.UserDTO;
import de.jadenk.springcloud.model.ApiToken;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.ApiTokenRepository;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.ApiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/*
    curl -k -X GET https://localhost:8080/api/log/2 -H "X-API-TOKEN: feb58cac1cbf427ea9efe12d114cb467" -H "Accept: application/json"
    für API-Endpunkte
 */

@RestController
@RequestMapping("/api") // Basis-URL für alle API-Endpunkte
public class ApiController {

    // Repositories für User, Logs und hochgeladene Dateien
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    /*
     * GET /api/users
     * Liefert alle Benutzer als JSON-Liste
     * Nur relevante Felder (ID, Username, Rolle) werden zurückgegeben
     */
    @GetMapping("/users")
    public List<Map<String, ? extends Serializable>> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Mapping der Benutzer auf Map für API-Ausgabe
        return users.stream()
                .map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "role", user.getRole() != null ? user.getRole().getName() : "NO_ROLE"
                ))
                .collect(Collectors.toList());
    }

    /*
     * GET /api/logs
     * Liefert alle Logs als DTO-Liste
     */
    @GetMapping("/logs")
    public List<LogDTO> getAllLogs() {
        return logRepository.findAll().stream()
                .map(LogDTO::new)
                .collect(Collectors.toList());
    }

    /*
     * GET /api/files
     * Liefert alle hochgeladenen Dateien als JSON-Liste
     */
    @GetMapping("/files")
    public List<Map<String, ? extends Serializable>> getAllFiles() {
        return uploadedFileRepository.findAll().stream()
                .map(file -> Map.of(
                        "id", file.getId(),
                        "fileName", file.getFileName(),
                        "fileType", file.getFileType()
                ))
                .collect(Collectors.toList());
    }

    /*
     * GET /api/user/{id}
     * Liefert einen einzelnen Benutzer nach ID
     * @return 200 OK mit UserDTO oder 404 Not Found
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * GET /api/log/{id}
     * Liefert einen einzelnen Log-Eintrag nach ID
     */
    @GetMapping("/log/{id}")
    public ResponseEntity<LogDTO> getLogById(@PathVariable Long id) {
        return logRepository.findById(id)
                .map(log -> ResponseEntity.ok(new LogDTO(log)))
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * GET /api/file/{id}
     * Liefert eine einzelne hochgeladene Datei nach ID
     */
    @GetMapping("/file/{id}")
    public ResponseEntity<UploadedFileDTO> getFileById(@PathVariable Long id) {
        return uploadedFileRepository.findById(id)
                .map(file -> ResponseEntity.ok(new UploadedFileDTO(file)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================
    // API Token Management (nur für Admin-Seite)
    // ==========================
    @Autowired
    private ApiTokenRepository apiTokenRepository;
    @Autowired
    private ApiTokenService apiTokenService;

    /*
     * POST /api/s/add
     * Fügt einen neuen API-Token hinzu
     * @param apiName - Name des Tokens
     */
    @PostMapping("/s/add")
    public String addApi(@RequestParam String apiName) throws IOException {
        ApiToken apiToken = new ApiToken();

        // Zufälligen Token generieren
        String token = UUID.randomUUID().toString().replace("-", "");
        apiToken.setToken(token);
        apiToken.setName(apiName);
        apiToken.setActive(true);

        // Speichern
        apiTokenRepository.save(apiToken);

        return "redirect:/admin"; // Weiterleitung zurück zur Admin-Seite
    }

    /*
     * POST /api/s/delete/{id}
     * Löscht einen API-Token nach ID
     */
    @PostMapping("/s/delete/{id}")
    public String deleteApiToken(@PathVariable Long id) {
        apiTokenService.deleteApiToken(id);
        return "redirect:/admin";
    }

    /*
     * POST /api/s/toggle/{id}
     * Aktiviert oder deaktiviert einen API-Token
     * @param active - Boolean, ob der Token aktiviert werden soll
     */
    @PostMapping("/s/toggle/{id}")
    public String toggleApiToken(@PathVariable Long id, @RequestParam(required = false) Boolean active, RedirectAttributes redirectAttributes) {
        apiTokenRepository.findById(id).ifPresent(apiToken -> {
            apiToken.setActive(active != null && active);
            apiTokenRepository.save(apiToken);
        });
        return "redirect:/admin";
    }

}
