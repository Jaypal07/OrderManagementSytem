package com.jaypal.oms.bootstrap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 *
 * Configures CORS policies for frontend applications to communicate with OMS API.
 * In development, allows all origins. In production, restricts to configured list.
 */
@Configuration
public class CorsConfig {

    @Value("${app.security.enable-cors:true}")
    private boolean enableCors;

    @Value("${app.security.cors-origins:http://localhost:3000,http://localhost:5173}")
    private String corsOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse comma-separated origins
        List<String> origins = Arrays.asList(corsOrigins.split(","));

        if (enableCors && !origins.contains("*")) {
            configuration.setAllowedOrigins(origins);
        } else if (enableCors) {
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        }

        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );
        configuration.setAllowedHeaders(
                Arrays.asList("*")
        );
        configuration.setExposedHeaders(
                Arrays.asList(
                        "X-Correlation-ID",
                        "X-Content-Type-Options",
                        "X-Frame-Options",
                        "Authorization"
                )
        );
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

