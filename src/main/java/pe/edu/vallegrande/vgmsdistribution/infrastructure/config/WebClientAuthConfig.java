package pe.edu.vallegrande.vgmsdistribution.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientAuthConfig {
    
    /**
     * Configuración de WebClient con interceptor de autenticación automática
     */
    @Bean
    public WebClient.Builder authenticatedWebClientBuilder() {
        return WebClient.builder()
                .filter(addAuthenticationHeader());
    }

    /**
     * Filtro que agrega automáticamente el header de Authorization con el token JWT
     */
    private ExchangeFilterFunction addAuthenticationHeader() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // Solo agregar el header si no existe ya
            if (clientRequest.headers().containsKey("Authorization")) {
                log.debug("Authorization header ya presente, no se modifica");
                return Mono.just(clientRequest);
            }

            return ReactiveSecurityContextHolder.getContext()
                    .map(securityContext -> securityContext.getAuthentication())
                    .cast(JwtAuthenticationToken.class)
                    .map(jwtAuth -> jwtAuth.getToken())
                    .map(Jwt::getTokenValue)
                    .map(token -> {
                        log.debug("Agregando token JWT automáticamente a la petición: {}", 
                                clientRequest.url().getPath());
                        
                        return ClientRequest.from(clientRequest)
                                .header("Authorization", "Bearer " + token)
                                .build();
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("No se encontró token JWT en el contexto para la petición: {}", 
                                clientRequest.url().getPath());
                        return Mono.just(clientRequest);
                    }))
                    .onErrorResume(error -> {
                        log.error("Error extrayendo token JWT para la petición: {}", error.getMessage());
                        return Mono.just(clientRequest);
                    });
        });
    }

    /**
     * WebClient específico para microservicios internos con autenticación automática
     */
    @Bean("internalWebClient")
    public WebClient internalWebClient() {
        return authenticatedWebClientBuilder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

}
