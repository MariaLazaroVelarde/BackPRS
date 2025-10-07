package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserResponse {
    private String userId;
    private String name;
    private String email;
    private String status;
}
