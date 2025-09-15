package de.jadenk.springcloud.config;

import de.jadenk.springcloud.model.ApiToken;
import de.jadenk.springcloud.repository.ApiTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {

    @Autowired
    private ApiTokenRepository apiTokenRepo;

    /**
     * Filtert alle eingehenden API-Anfragen unter /api/ außer /api/s/ (Administrative Endpoints),
     * überprüft den X-API-TOKEN Header und validiert ihn gegen die Datenbank.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Nur öffentliche API-Endpunkte filtern
        if (path.startsWith("/api/") && !path.startsWith("/api/s/")) {
            String token = request.getHeader("X-API-TOKEN");

            // Prüfen, ob Token vorhanden ist
            if (token == null || token.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing API Token");
                return;
            }

            // Prüfen, ob Token gültig und aktiv ist
            Optional<ApiToken> validToken = apiTokenRepo.findByTokenAndActiveTrue(token);
            if (validToken.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Invalid API Token");
                return;
            }
        }

        // Token gültig oder nicht erforderlich -> Filterkette fortsetzen
        filterChain.doFilter(request, response);
    }
}
