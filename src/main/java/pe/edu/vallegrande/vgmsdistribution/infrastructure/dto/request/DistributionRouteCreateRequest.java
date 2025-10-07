package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteCreateRequest {

    private String organizationId;
    private String routeCode;
    private String routeName;
    private String zoneId;
    private String streetId;  
    private Integer totalEstimatedDuration; // en horas
    private String responsibleUserId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneEntry {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration; // en horas
    }
}