package com.jaypal.oms.bootstrap.observability;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Correlation ID Filter
 *
 * Extracts or generates a correlation ID for each request.
 * Stores it in MDC for logging propagation across the entire request lifecycle.
 * Also adds it to response headers for distributed tracing.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract correlation ID from request header or generate new one
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Store in MDC for logging
        MDC.put(MDC_KEY, correlationId);

        try {
            // Add to response headers for downstream services
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(MDC_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for actuator and static resources
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/webjars") ||
               path.startsWith("/css") ||
               path.startsWith("/js");
    }
}

