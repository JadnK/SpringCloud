package de.jadenk.springcloud.security;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.util.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private LogService logService; // Service zum Speichern von Logeinträgen

    @Autowired
    private UserRepository userRepo; // Repository zum Zugriff auf User-Daten

    @Autowired
    private MessageService messageService; // Service für Nachrichten-Templates

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Benutzername aus dem Authentication-Objekt extrahieren
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Benutzer aus der Datenbank abrufen
        User user = userRepo.findByUsername(username).orElse(null);

        if (user != null) {
            // Fehlversuche zurücksetzen, da Login erfolgreich war
            user.setFailedLoginAttempts(0);

            // Eventuelle Sperrzeit zurücksetzen
            user.setLockoutTime(null);

            // Änderungen speichern
            userRepo.save(user);

            // Erfolgreiches Login in LogService protokollieren
            logService.log(user.getUsername(), messageService.getLog("login.success"));
        }

        // Nach erfolgreichem Login auf das Dashboard weiterleiten
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
