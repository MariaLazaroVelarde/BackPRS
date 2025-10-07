package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareCreateRequest {

    private String organizationId;
    private String fareCode;
    private String fareName;
    private String fareType; // DIARIA, SEMANAL, MENSUAL
    private BigDecimal fareAmount;
    private Instant effectiveDate; // New field for time-based fare changes
}