package pe.edu.vallegrande.vgmsdistribution.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Qualifier("internalWebClient")
    private final WebClient internalWebClient;

    @Value("${microservices.organization.url}")
    private String organizationServiceUrl;

    /**
     * Obtiene la información de una organización por su ID
     * @param organizationId ID de la organización
     * @return Mono con la información de la organización
     */
    public Mono<OrganizationResponse> getOrganizationById(String organizationId) {
        log.info("Obteniendo información de la organización con ID: {}", organizationId);
        
        return getJwtToken()
                .flatMap(token -> {
                    WebClient webClient = webClientBuilder.build();
                    
                    return webClient.get()
                            .uri(organizationServiceUrl + "/organization/{organizationId}", organizationId)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(OrganizationApiResponse.class)
                            .map(OrganizationApiResponse::getData)
                            .doOnNext(org -> log.info("Organización obtenida: {}", org))
                            .onErrorResume(error -> {
                                log.error("Error obteniendo organización: {}", error.getMessage());
                                return Mono.empty();
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se pudo obtener el token JWT, intentando sin autenticación");
                    WebClient webClient = webClientBuilder.build();
                    
                    return webClient.get()
                            .uri(organizationServiceUrl + "/organization/{organizationId}", organizationId)
                            .retrieve()
                            .bodyToMono(OrganizationApiResponse.class)
                            .map(OrganizationApiResponse::getData)
                            .doOnNext(org -> log.info("Organización obtenida sin token: {}", org))
                            .onErrorResume(error -> {
                                log.error("Error obteniendo organización sin token: {}", error.getMessage());
                                return Mono.empty();
                            });
                }));
    }

    /**
     * Obtiene la información de una organización por su ID usando WebClient con auth automática
     * @param organizationId ID de la organización
     * @return Mono con la información de la organización
     */
    public Mono<OrganizationResponse> getOrganizationByIdAutoAuth(String organizationId) {
        log.info("Obteniendo información de la organización con ID: {} (auth automática)", organizationId);
        
        return internalWebClient.get()
                .uri(organizationServiceUrl + "/organization/{organizationId}", organizationId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), 
                    response -> {
                        log.error("Error del servidor (5xx) al obtener organización {}: {}", organizationId, response.statusCode());
                        return Mono.error(new RuntimeException("Servicio de organización no disponible: " + response.statusCode()));
                    })
                .onStatus(status -> status.is4xxClientError(),
                    response -> {
                        log.warn("Error del cliente (4xx) al obtener organización {}: {}", organizationId, response.statusCode());
                        return Mono.error(new RuntimeException("Organización no encontrada o acceso denegado: " + response.statusCode()));
                    })
                .bodyToMono(OrganizationApiResponse.class)
                .map(OrganizationApiResponse::getData)
                .doOnNext(org -> log.info("Organización obtenida con auth automática: {}", org))
                .onErrorResume(error -> {
                    log.error("Error obteniendo organización con auth automática: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Extrae el token JWT del contexto de seguridad de Spring
     * @return Mono con el token JWT como String
     */
    private Mono<String> getJwtToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> jwtAuth.getToken())
                .map(Jwt::getTokenValue)
                .doOnNext(token -> log.debug("Token JWT extraído del contexto de seguridad"))
                .onErrorResume(error -> {
                    log.warn("No se pudo extraer el token JWT del contexto: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    // Clases para mapear la respuesta del microservicio de organizaciones
    public static class OrganizationApiResponse {

        private boolean success;
        private OrganizationResponse data;
        private String message;

        // Getters y setters
        public boolean isSuccess() { return success; }

        public void setSuccess(boolean success) { this.success = success; }

        public OrganizationResponse getData() { return data; }

        public void setData(OrganizationResponse data) { this.data = data; }

        public String getMessage() { return message; }

        public void setMessage(String message) { this.message = message; }
    }

    public static class OrganizationResponse {

        private String organizationId;
        private String organizationCode;
        private String organizationName;

        // Getters y setters
        public String getOrganizationId() { return organizationId; }

        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

        public String getOrganizationCode() { return organizationCode; }

        public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }

        public String getOrganizationName() { return organizationName; }

        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    }

}