package com.jaypal.oms.bootstrap.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Converts JWT roles claim into Spring Security authorities.
 * Expects roles to be in the claim "roles" as a list of strings or a single string.
 */
@Component
public class JwtConverter {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (defaultAuthorities != null) {
                    authorities.addAll(defaultAuthorities);
                }

                Object roles = jwt.getClaims().get("roles");
                if (roles instanceof Iterable<?>) {
                    List<String> roleStrings = StreamSupport.stream(((Iterable<?>) roles).spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    roleStrings.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                } else if (roles instanceof String) {
                    String r = (String) roles;
                    String val = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                    authorities.add(new SimpleGrantedAuthority(val));
                }

                return authorities;
            }
        });
        return conv;
    }
}
