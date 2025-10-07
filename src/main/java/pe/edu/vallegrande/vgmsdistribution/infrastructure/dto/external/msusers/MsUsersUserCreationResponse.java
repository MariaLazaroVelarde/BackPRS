package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para los datos de creación de usuario dentro de ApiResponse de MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsUsersUserCreationResponse {
    private MsUsersUserInfo userInfo;
    private String username;
    private String password;
    // Añadir otros campos si son relevantes y vienen en la respuesta de MS-USERS
}
