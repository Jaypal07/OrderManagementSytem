package com.jaypal.oms.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.jaypal.oms.order.api.SecurityTestController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityTestController.class)
@Import(SecurityTestController.class)
public class SecurityIntegrationTest {

    static KeyPair keyPair;
    static RSAKey rsaJWK;

    @BeforeAll
    static void setupKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        rsaJWK = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID("test-key-1")
                .build();
    }

    @Autowired
    MockMvc mvc;

    @Test
    void adminEndpoint_allows_admin_with_valid_jwt() throws Exception {
        String token = createToken(rsaJWK);

        mvc.perform(get("/security/admin")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("admin ok"));
    }

    private String createToken(RSAKey rsaJWK) throws JOSEException {
        JWSSigner signer = new RSASSASigner(rsaJWK.toPrivateKey());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("http://iam.example")
                .subject("user-123")
                .claim("roles", new String[]{"ADMIN"})
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaJWK.getKeyID())
                .build(), claimsSet);

        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        }
    }
}
