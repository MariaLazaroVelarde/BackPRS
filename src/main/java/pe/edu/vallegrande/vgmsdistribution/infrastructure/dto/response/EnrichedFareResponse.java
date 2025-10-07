package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedFareResponse {
    private String id;
    private String organizationId;
    private OrganizationResponse organization; // Organization details
    private String fareCode;
    private String fareName;
    private String fareType;
    private BigDecimal fareAmount;
    private String status;
    private Instant createdAt;
}