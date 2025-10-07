package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;
import pe.edu.vallegrande.vgmsdistribution.domain.enums.Constants;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrganizationWithAdminsResponse {
    private String organizationId;
    private String organizationCode;
    private String organizationName;
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
    private Constants status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AdminUserResponse> admins;
}
