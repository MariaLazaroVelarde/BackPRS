package pe.edu.vallegrande.vgmsdistribution.infrastructure.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PermissionValidationService {
    
    /**
     * Valida si el usuario tiene el rol especificado usando Spring Security Context
     * @param exchange El intercambio de la solicitud
     * @param requiredRole El rol requerido
     * @return Mono<Boolean> que indica si el usuario tiene el rol
     */
    public Mono<Boolean> validateRole(ServerWebExchange exchange, String requiredRole) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .cast(Authentication.class)
                .map(authentication -> {
                    // Verificar si el usuario tiene el rol requerido
                    boolean hasRole = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(authority -> 
                                authority.equals("ROLE_" + requiredRole) || 
                                authority.equals(requiredRole)
                            );
                    return hasRole;
                })
                .defaultIfEmpty(false);
    }

    /**
     * Método alternativo que sigue usando headers (para compatibilidad)
     * @param exchange El intercambio de la solicitud
     * @param requiredRole El rol requerido
     * @return Mono<Boolean> que indica si el usuario tiene el rol
     */
    public Mono<Boolean> validateRoleFromHeader(ServerWebExchange exchange, String requiredRole) {
        List<String> roles = exchange.getRequest().getHeaders().get("X-Roles");
        
        if (roles == null || roles.isEmpty()) {
            return Mono.just(false);
        }
        
        // Verificar si alguna de las cadenas en la lista contiene el rol requerido
        boolean hasRole = roles.stream()
                .anyMatch(roleString -> roleString.contains(requiredRole));
        
        return Mono.just(hasRole);
    }

}