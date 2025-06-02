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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

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
        if (username != null) {
            userRepo.findByUsername(username).ifPresent(user -> {

                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);

                if (attempts >= cloudSettingService.getNumberSetting("MAX_LOGIN_ATTEMPTS", 3)) {
                    user.setLockoutTime(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION));
                }
                userRepo.save(user);
            });
        }

        response.sendRedirect("/login?error");
    }

}
