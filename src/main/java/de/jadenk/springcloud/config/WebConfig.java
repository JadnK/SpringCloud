package de.jadenk.springcloud.config;

import de.jadenk.springcloud.security.BannedUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BannedUserInterceptor bannedUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bannedUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/banned", "/logout", "/login", "/css/**", "/js/**");
    }
}
