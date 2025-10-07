package pe.edu.vallegrande.vgmsdistribution.domain.models;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "routes")
public class DistributionRoute {
    @Id
    private String id;
    private String organizationId;
    private String routeCode;
    private String routeName;
    private String zones;
    private int totalEstimatedDuration; 
    private String responsibleUserId;
    private String status;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneOrder {
        private String zoneId;
        private int order;
        private int estimatedDuration; // en horas
    }
}
