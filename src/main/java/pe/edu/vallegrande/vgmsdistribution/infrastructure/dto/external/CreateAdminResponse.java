package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta del servicio MS-USERS al crear administradores
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // <--- Added this annotation
public class CreateAdminResponse {
    
    private String userId;
    private String name;
    private String email;
    private String role;
    private String organizationId;
    private String status;
    private LocalDateTime createdAt;
    private boolean success;
    private String message;
}
