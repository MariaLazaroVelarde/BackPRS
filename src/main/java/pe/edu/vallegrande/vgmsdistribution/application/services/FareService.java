package pe.edu.vallegrande.vgmsdistribution.application.services;

import pe.edu.vallegrande.vgmsdistribution.domain.models.Fare;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedFareResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FareService {
    
    Flux<Fare> getAllF();
    
    Flux<Fare> getAllActiveF();
    
    Flux<Fare> getAllInactiveF();
    
    Mono<Fare> getByIdFMono(String id);
    
    Mono<FareResponse> saveF(FareCreateRequest request);
    
    Mono<Fare> updateF(String id, Fare fare);
    
    Mono<Void> deleteF(String id);
    
    Mono<Fare> activateF(String id);
    
    Mono<Fare> deactivateF(String id);
    
    // New methods for enriched fare data
    Mono<EnrichedFareResponse> getEnrichedById(String id);
    
    Flux<EnrichedFareResponse> getAllEnriched();
    
    Flux<EnrichedFareResponse> getAllActiveEnriched();
    
    Flux<EnrichedFareResponse> getAllInactiveEnriched();
    
    // Method to get current active fare based on effective date
    Mono<Fare> getCurrentActiveFare(String organizationId);
}