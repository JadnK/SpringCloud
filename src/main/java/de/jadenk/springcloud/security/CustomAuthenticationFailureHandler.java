package de.jadenk.springcloud.security;

import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.CloudSettingService;
import de.jadenk.springcloud.service.LogService;
import de.jadenk.springcloud.util.MessageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private LogService logService; // Service für Logging von Ereignissen

    @Autowired
    private UserRepository userRepo; // Repository für Zugriff auf User-Daten

    @Autowired
    private MessageService messageService; // Service für Fehler- und Lognachrichten

    @Autowired
    private CloudSettingService cloudSettingService; // Service für App-Einstellungen

    // Dauer der Sperre nach zu vielen Fehlversuchen (in Minuten)
    private static final long LOCK_TIME_DURATION = 5;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        // Username aus dem Login-Formular extrahieren
        String username = request.getParameter("username");

        // Flag, ob der Account gesperrt wurde
        AtomicBoolean locked = new AtomicBoolean(false);

        if (username != null) {
            // Benutzer aus der Datenbank holen, falls vorhanden
            userRepo.findByUsername(username).ifPresent(user -> {

                // Max. Login-Versuche aus Cloud-Einstellungen holen (default 3)
                int maxAttempts = cloudSettingService.getNumberSetting("MAX_LOGIN_ATTEMPTS", 3);
                LocalDateTime now = LocalDateTime.now();

                // Prüfen, ob der Benutzer bereits gesperrt ist
                if (user.getLockoutTime() != null && user.getLockoutTime().isAfter(now)) {
                    locked.set(true);
                } else {
                    // Fehlversuche erhöhen
                    int attempts = user.getFailedLoginAttempts();
                    attempts++;

                    // Prüfen, ob max. Versuche erreicht wurden
                    if (attempts >= maxAttempts) {
                        // Account sperren für LOCK_TIME_DURATION Minuten
                        user.setLockoutTime(now.plusMinutes(LOCK_TIME_DURATION));
                        user.setFailedLoginAttempts(0); // Reset der Fehlversuche
                        locked.set(true);
                    } else {
                        // Fehlversuche aktualisieren
                        user.setFailedLoginAttempts(attempts);
                    }

                    // Änderungen speichern
                    userRepo.save(user);
                }
            });
        }

        // Redirect auf Login-Seite mit entsprechender Fehlermeldung
        if (locked.get()) {
            response.sendRedirect(request.getContextPath() + "/login?error=locked");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error");
        }
    }
}
