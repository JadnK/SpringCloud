package de.jadenk.springcloud.security;

import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BannedUserInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserDetails) {

            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.getUserByName(username);

            if (user != null && user.isBanned() && !request.getRequestURI().startsWith("/banned")) {
                response.sendRedirect("/banned");
                return false;
            }
        }

        return true;
    }
}
