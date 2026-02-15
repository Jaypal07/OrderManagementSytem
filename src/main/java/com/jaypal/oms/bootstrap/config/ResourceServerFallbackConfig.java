package com.jaypal.oms.bootstrap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@Configuration
public class ResourceServerFallbackConfig {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        // Fallback decoder used when no jwk/public-key configuration is provided.
        // It always rejects tokens which is the safest default for unsecured test contexts.
        return token -> {
            throw new JwtException("No JwtDecoder configured for Resource Server. Set spring.security.oauth2.resourceserver.jwt.jwk-set-uri or provide a JwtDecoder bean for tests.");
        };
    }
}

