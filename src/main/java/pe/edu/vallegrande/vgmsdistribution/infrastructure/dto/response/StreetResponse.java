package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetResponse {
    private String streetId;
    private String zoneId;
    private String streetCode;
    private String streetName;
    private String streetType;
    private String status;
    private Instant createdAt;
}
