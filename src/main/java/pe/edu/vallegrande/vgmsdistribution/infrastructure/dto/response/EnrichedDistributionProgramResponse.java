package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.service.OrganizationService;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedDistributionProgramResponse {
    
    private String id;
    private String organizationId;
    private OrganizationService.OrganizationResponse organization; // Organization details
    private String programCode;
    private String scheduleId;
    private String routeId;
    private String zoneId;
    private String streetId;
    private LocalDate programDate;
    private String plannedStartTime;
    private String plannedEndTime;
    private String actualStartTime;
    private String actualEndTime;
    private String status;
    private String responsibleUserId;
    private String observations;
    private Instant createdAt;
}