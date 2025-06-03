package de.jadenk.springcloud.config;

import de.jadenk.springcloud.security.CustomAuthenticationFailureHandler;
import de.jadenk.springcloud.security.CustomAuthenticationSuccessHandler;
import de.jadenk.springcloud.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ApiTokenFilter apiTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        //.deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/share/**",
                                "/link-expired"
                        ).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated())
                .rememberMe(remember -> remember
                        .key("cookie_remember_me_jadenk_292929")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(failureHandler)
                        .successHandler(successHandler)
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login")
                        .maximumSessions(10)
                        .sessionRegistry(sessionRegistry())
                );
        http.addFilterBefore(apiTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
