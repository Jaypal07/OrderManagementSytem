package com.jaypal.oms.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for JWT flow validation
 * Provides endpoints with different role-based access controls
 */
@RestController
@RequestMapping("/test")
public class JwtTestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Admin access granted";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String userEndpoint() {
        return "User access granted";
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public access";
    }
}

