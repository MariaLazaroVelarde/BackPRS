package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.service.OrganizationService;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedDistributionScheduleResponse {
    
    private String id;
    private String organizationId;
    private OrganizationService.OrganizationResponse organization; // Organization details
    private String scheduleCode;
    private String zoneId;
    private String streetId;
    private String scheduleName;
    private List<String> daysOfWeek;
    private String startTime;
    private String endTime;
    private Integer durationHours;
    private String status;
    private Instant createdAt;
}