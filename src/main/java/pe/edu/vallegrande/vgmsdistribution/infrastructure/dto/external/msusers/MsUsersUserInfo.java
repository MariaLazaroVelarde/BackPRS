package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la información de usuario dentro de UserCreationResponse de MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsUsersUserInfo {
    private String id; // Renombrado de userId a id para coincidir con el JSON de MS-USERS
    private String userCode;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String name; // Añadido para mapear el nombre completo si viene
    private String status; // Añadido para mapear el estado del usuario
    // Añadir otros campos si son relevantes y vienen en la respuesta de MS-USERS
}