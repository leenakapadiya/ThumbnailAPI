package com.thumbnailapi.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for CORS and other web settings.
 *
 * CORS allowed origins are configured via the {@code cors.allowed-origins}
 * application property.  An empty list means no cross-origin requests are
 * permitted (secure default).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = allowedOrigins == null ? List.of() :
                allowedOrigins.stream().filter(s -> s != null && !s.isBlank()).toList();
        if (origins.isEmpty()) {
            // No origins configured – cross-origin requests are denied by default
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST")
                .maxAge(3600);
    }
}
