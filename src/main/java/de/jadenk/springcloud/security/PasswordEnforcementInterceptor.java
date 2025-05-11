package de.jadenk.springcloud.security;

import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class PasswordEnforcementInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String defaultPassword = "jadenk_§!";

    public PasswordEnforcementInterceptor(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.startsWith("/login") || path.startsWith("/logout") || path.startsWith("/change-password")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (passwordEncoder.matches(defaultPassword, user.getPassword())) {
                    response.sendRedirect("/change-password");
                    return false;
                }
            }
        }
        return true;
    }
}
