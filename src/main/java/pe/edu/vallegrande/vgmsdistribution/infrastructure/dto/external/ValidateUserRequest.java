package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para validar existencia de usuarios en el servicio MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    private String userId;
}