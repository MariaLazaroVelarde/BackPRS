package pe.edu.vallegrande.vgmsdistribution.infrastructure.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JwtService {
    
    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtService(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Valida un token JWT
     * @param token El token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Firma JWT inválida: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            return false;
        } catch (JwtValidationException e) {
            log.error("Error de validación JWT: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("Error en token JWT: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae los roles del token JWT
     * @param token El token JWT
     * @return Lista de roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            // Usar el JwtDecoder de Spring Security para obtener los claims
            var jwt = jwtDecoder.decode(token);
            Map<String, Object> claims = jwt.getClaims();

            // Intentar extraer roles de realm_access (formato Keycloak)
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    log.debug("Roles extraídos de realm_access: {}", roles);
                    return roles;
                }
            }

            // Intentar extraer roles de resource_access (otro formato común)
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            if (resourceAccess != null) {
                for (Object resourceObj : resourceAccess.values()) {
                    if (resourceObj instanceof Map) {
                        Map<String, Object> resource = (Map<String, Object>) resourceObj;
                        if (resource.containsKey("roles")) {
                            List<String> roles = (List<String>) resource.get("roles");
                            if (roles != null && !roles.isEmpty()) {
                                log.debug("Roles extraídos de resource_access: {}", roles);
                                return roles;
                            }
                        }
                    }
                }
            }

            // Intentar extraer del claim "roles" directo
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                List<String> roles = (List<String>) rolesObj;
                log.debug("Roles extraídos del claim 'roles': {}", roles);
                return roles;
            }

            log.warn("No se encontraron roles en el token JWT");
            return new ArrayList<>();
        } catch (ClassCastException e) {
            log.error("Error de conversión al extraer roles: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error al extraer roles del token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

}
