package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta del servicio MS-USERS al validar usuarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserResponse {
    
    private boolean exists;
    private String userId;
    private String email;
    private String status;
    private String message;
}