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
    private LogService logService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CloudSettingService cloudSettingService;

    private static final long LOCK_TIME_DURATION = 5;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        AtomicBoolean locked = new AtomicBoolean(false);

        if (username != null) {
            userRepo.findByUsername(username).ifPresent(user -> {

                int maxAttempts = cloudSettingService.getNumberSetting("MAX_LOGIN_ATTEMPTS", 3);
                LocalDateTime now = LocalDateTime.now();

                if (user.getLockoutTime() != null && user.getLockoutTime().isAfter(now)) {
                    locked.set(true);
                } else {
                    int attempts = user.getFailedLoginAttempts();
                    attempts++;

                    if (attempts >= maxAttempts) {
                        user.setLockoutTime(now.plusMinutes(LOCK_TIME_DURATION));
                        user.setFailedLoginAttempts(0);
                        locked.set(true);
                    } else {
                        user.setFailedLoginAttempts(attempts);
                    }

                    userRepo.save(user);
                }
            });
        }

        if (locked.get()) {
            response.sendRedirect(request.getContextPath() + "/login?error=locked");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error");
        }
    }




}
