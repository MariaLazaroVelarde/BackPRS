package pe.edu.vallegrande.vgmsdistribution.infrastructure.adapter.out;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.vgmsdistribution.application.config.MsUsersConfig;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.CreateAdminRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.CreateAdminResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.ValidateUserRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.ValidateUserResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers.ApiResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers.MsUsersUserCreationResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers.MsUsersUserInfo;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.exception.CustomException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Cliente para comunicarse con el microservicio MS-USERS
 * Maneja las operaciones de creación de administradores, validación de usuarios
 * y obtención de datos de usuarios con sus organizaciones
 */
@Component
@Slf4j
public class UserAuthClient {

    private final WebClient webClient;
    private final MsUsersConfig msUsersConfig;

    public UserAuthClient(@Qualifier("msUsersWebClient") WebClient webClient, MsUsersConfig msUsersConfig) {
        this.webClient = webClient;
        this.msUsersConfig = msUsersConfig;
    }

    /**
     * Obtiene administradores por organización
     */
    public Flux<MsUsersUserInfo> getAdminsByOrganizationId(String organizationId) {
        String endpoint = msUsersConfig.getEndpoints().getAdmins()
                .replace("{organizationId}", organizationId);
        
        log.debug("Getting admins for organization: {} using endpoint: {}", organizationId, endpoint);
        
        return webClient
                .get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<MsUsersUserInfo>>>() {})
                .flatMapMany(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Flux.fromIterable(apiResponse.getData());
                    } else {
                        return Flux.empty();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error getting admins for organization {}: {}", organizationId, ex.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Obtiene usuarios por organización
     */
    public Flux<MsUsersUserInfo> getUsersByOrganizationId(String organizationId) {
        String endpoint = msUsersConfig.getEndpoints().getUsers()
                .replace("{organizationId}", organizationId);
        
        return webClient
                .get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<MsUsersUserInfo>>>() {})
                .flatMapMany(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Flux.fromIterable(apiResponse.getData());
                    } else {
                        return Flux.empty();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error getting users for organization {}: {}", organizationId, ex.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Obtiene clientes por organización
     */
    public Flux<MsUsersUserInfo> getClientsByOrganizationId(String organizationId) {
        String endpoint = msUsersConfig.getEndpoints().getClients()
                .replace("{organizationId}", organizationId);
        
        return webClient
                .get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<MsUsersUserInfo>>>() {})
                .flatMapMany(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Flux.fromIterable(apiResponse.getData());
                    } else {
                        return Flux.empty();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error getting clients for organization {}: {}", organizationId, ex.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Obtiene usuario por ID
     */
    public Mono<MsUsersUserInfo> getUserById(String userId) {
        String endpoint = msUsersConfig.getEndpoints().getUserById()
                .replace("{userId}", userId);
        
        return webClient
                .get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<MsUsersUserInfo>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Mono.just(apiResponse.getData());
                    } else {
                        return Mono.empty();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error getting user {}: {}", userId, ex.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Crea un usuario administrador en el servicio MS-USERS
     */
    public Mono<CreateAdminResponse> createAdmin(CreateAdminRequest request, String organizationId) {
        log.debug("Creating admin user in MS-USERS: {}", request.getEmail());
        
        String endpoint = msUsersConfig.getEndpoints().getCreateAdmin()
                .replace("{organizationId}", organizationId);
        
        return webClient
                .post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<MsUsersUserCreationResponse>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                        MsUsersUserCreationResponse userCreationData = apiResponse.getData();
                        MsUsersUserInfo userInfo = userCreationData.getUserInfo();

                        if (userInfo != null && (userInfo.getId() != null || userInfo.getUserCode() != null)) {
                            String userIdToUse = userInfo.getId() != null ? userInfo.getId() : userInfo.getUserCode();

                            return Mono.just(CreateAdminResponse.builder()
                                    .userId(userIdToUse)
                                    .name(userInfo.getName())
                                    .email(userInfo.getEmail())
                                    .success(apiResponse.isSuccess())
                                    .message(apiResponse.getMessage())
                                    .build());
                        } else {
                            return Mono.error(new CustomException(
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Error from MS-USERS",
                                    "Missing user info or valid ID."));
                        }
                    } else {
                        return Mono.error(new CustomException(
                                HttpStatus.BAD_REQUEST.value(),
                                "MS-USERS returned failure",
                                "No details available."));
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Valida la existencia de un usuario por email
     */
    public Mono<Boolean> validateUserByEmail(String email) {
        ValidateUserRequest request = new ValidateUserRequest();
        request.setEmail(email);
        return validateUser(request).map(ValidateUserResponse::isExists);
    }

    /**
     * Valida la existencia de un usuario por ID
     */
    public Mono<Boolean> validateUserById(String userId) {
        ValidateUserRequest request = new ValidateUserRequest();
        request.setUserId(userId);
        return validateUser(request).map(ValidateUserResponse::isExists);
    }

    /**
     * Valida la existencia de un usuario en el servicio MS-USERS
     */
    private Mono<ValidateUserResponse> validateUser(ValidateUserRequest request) {
        return webClient
                .post()
                .uri(msUsersConfig.getEndpoints().getValidate())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ValidateUserResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException);
    }

    /**
     * Mapea las excepciones de WebClient a excepciones personalizadas
     */
    private CustomException mapWebClientException(WebClientResponseException ex) {
        String errorMessage = "MS-USERS service error";
        
        switch (ex.getStatusCode().value()) {
            case 400:
                errorMessage = "Bad request to MS-USERS service";
                break;
            case 404:
                errorMessage = "MS-USERS service endpoint not found";
                break;
            case 500:
                errorMessage = "MS-USERS service internal error";
                break;
            case 503:
                errorMessage = "MS-USERS service unavailable";
                break;
            default:
                errorMessage = "MS-USERS service returned error: " + ex.getStatusCode();
        }
        
        return new CustomException(
                ex.getStatusCode().value(),
                errorMessage,
                ex.getResponseBodyAsString());
    }
    
    /**
     * Obtiene detalles de la organización por ID
     */
    public Mono<OrganizationResponse> getOrganizationById(String organizationId) {
        String endpoint = msUsersConfig.getEndpoints().getOrganizationById()
                .replace("{organizationId}", organizationId);
        
        log.debug("Getting organization details: {} using endpoint: {}", organizationId, endpoint);
        
        return webClient
                .get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<OrganizationResponse>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Mono.just(apiResponse.getData());
                    } else {
                        return Mono.empty();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error getting organization {}: {}", organizationId, ex.getMessage());
                    return Mono.empty();
                });
    }
}