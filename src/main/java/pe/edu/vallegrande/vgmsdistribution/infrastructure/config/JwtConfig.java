package pe.edu.vallegrande.vgmsdistribution.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

@Configuration
public class JwtConfig  {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            try {
                return ReactiveJwtDecoders.fromIssuerLocation(issuerUri)
                        .decode(token)
                        .block(); // Usar block() en lugar de toFuture().get()
            } catch (Exception e) {
                throw new RuntimeException("Error decoding JWT token", e);
            }
        };
    }
    
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

}
