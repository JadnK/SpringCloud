package de.jadenk.springcloud.config;

import de.jadenk.springcloud.security.BannedUserInterceptor;
import de.jadenk.springcloud.security.PasswordEnforcementInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BannedUserInterceptor bannedUserInterceptor;

    @Autowired
    private PasswordEnforcementInterceptor passwordEnforcementInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor für gesperrte Benutzer
        registry.addInterceptor(bannedUserInterceptor)
                .addPathPatterns("/**") // alle Pfade
                .excludePathPatterns(
                        "/banned", "/logout", "/login", "/css/**", "/js/**"
                ); // Ausnahmen: Login, Logout, statische Ressourcen, Banned-Seite

        // Interceptor für Passwort-Policy Enforcement
        registry.addInterceptor(passwordEnforcementInterceptor);
    }

    // CORS-Konfiguration (auskommentiert)
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins("http://127.0.0.1:8080")
    //             .allowedMethods("GET", "POST")
    //             .allowedHeaders("*")
    //             .allowCredentials(true);
    // }
}
