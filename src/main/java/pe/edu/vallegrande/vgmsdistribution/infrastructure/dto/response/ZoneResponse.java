package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ZoneResponse {
    private String zoneId;
    private String organizationId;
    private String zoneCode;
    private String zoneName;
    private String description;
    private String status;
    private List<StreetResponse> streets;
}