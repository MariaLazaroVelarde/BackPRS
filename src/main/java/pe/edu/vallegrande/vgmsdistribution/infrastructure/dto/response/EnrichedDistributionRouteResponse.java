package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.service.OrganizationService;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedDistributionRouteResponse {
    private String id;
    private String organizationId;
    private OrganizationService.OrganizationResponse organization; // Organization details
    private String routeCode;
    private String routeName;
    private String zoneId;
    private Integer totalEstimatedDuration;
    private String responsibleUserId;
    private String status;
    private Instant createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneDetail {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration;
    }
}