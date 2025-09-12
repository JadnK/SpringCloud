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
    private LogService logService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageService messageService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepo.findByUsername(username).orElse(null);

        // System.out.println("Authentication Object: " + authentication);

        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLockoutTime(null);
            userRepo.save(user);

            logService.log(user.getUsername(), messageService.getLog("login.success"));
        }


        response.sendRedirect(request.getContextPath() + "/dashboard");

    }
}
