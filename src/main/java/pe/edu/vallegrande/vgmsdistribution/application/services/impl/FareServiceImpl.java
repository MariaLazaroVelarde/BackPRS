package pe.edu.vallegrande.vgmsdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.vgmsdistribution.application.services.FareService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.Fare;
import pe.edu.vallegrande.vgmsdistribution.domain.enums.Constants;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedFareResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class FareServiceImpl implements FareService {

    @Autowired
    private FareRepository fareRepository;

    @Override
    public Flux<Fare> getAllF() {
        return fareRepository.findAll()
                .doOnNext(fare -> System.out.println("Fare retrieved: " + fare));
    }

    @Override
    public Flux<Fare> getAllActiveF() {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<Fare> getAllInactiveF() {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<Fare> getByIdFMono(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Fare not found",
                        "The requested fare with id " + id + " was not found")));
    }

    @Override
    public Mono<FareResponse> saveF(FareCreateRequest request) {
        return generateNextFareCode() // ← Aquí lo usamos
                .flatMap(generatedCode -> fareRepository.existsByFareCode(generatedCode)
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new CustomException(
                                        HttpStatus.BAD_REQUEST.value(),
                                        "Fare code already exists",
                                        "The fare code " + generatedCode + " is already registered"));
                            }

                            // Determine the initial status based on effective date
                            String initialStatus = Constants.ACTIVE.name();
                            Instant effectiveDate = request.getEffectiveDate();
                            Instant now = Instant.now();
                            
                            // If effective date is in the future, set status to INACTIVE initially
                            if (effectiveDate != null && effectiveDate.isAfter(now)) {
                                initialStatus = Constants.INACTIVE.name();
                            }

                            Fare fare = Fare.builder()
                                    .organizationId(request.getOrganizationId())
                                    .fareCode(generatedCode) // ← Se usa el código generado
                                    .fareName(request.getFareName())
                                    .fareType(request.getFareType())
                                    .fareAmount(request.getFareAmount())
                                    .effectiveDate(request.getEffectiveDate()) // Set effective date
                                    .status(initialStatus) // Set initial status based on effective date
                                    .createdAt(Instant.now())
                                    .build();

                            return fareRepository.save(fare)
                                    .flatMap(savedFare -> {
                                        // If this fare has an effective date in the past or present, 
                                        // handle time-based activation immediately
                                        if (savedFare.getEffectiveDate() != null && 
                                            !savedFare.getEffectiveDate().isAfter(Instant.now())) {
                                            return handleTimeBasedFareActivation(savedFare);
                                        }
                                        return Mono.just(savedFare);
                                    })
                                    .map(savedFare -> FareResponse.builder()
                                            .id(savedFare.getId())
                                            .organizationId(savedFare.getOrganizationId())
                                            .fareCode(savedFare.getFareCode())
                                            .fareName(savedFare.getFareName())
                                            .fareType(savedFare.getFareType())
                                            .fareAmount(savedFare.getFareAmount())
                                            .status(savedFare.getStatus())
                                            .createdAt(savedFare.getCreatedAt())
                                            .build());
                        }));
    }

    /**
     * Handles time-based fare activation:
     * - Deactivates current active fares that will be replaced by this new fare
     * - Schedules future activation if the effective date is in the future
     */
    private Mono<Fare> handleTimeBasedFareActivation(Fare newFare) {
        // For simplicity, we'll deactivate all active fares for the same organization
        // In a more complex system, you might want to filter by fare type or other criteria
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getOrganizationId().equals(newFare.getOrganizationId()))
                .flatMap(fare -> {
                    // Deactivate the current fare
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                })
                .then(Mono.just(newFare));
    }

    private static final String FARE_PREFIX = "TAR";

    private Mono<String> generateNextFareCode() {
        return fareRepository.findTopByOrderByFareCodeDesc()
                .map(last -> {
                    String lastCode = last.getFareCode(); // ej. "TAR003"
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(FARE_PREFIX, ""));
                    } catch (NumberFormatException e) {
                        // Si el código no sigue el patrón, asumimos 0
                    }
                    return String.format(FARE_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(FARE_PREFIX + "001");
    }

    @Override
    public Mono<Fare> updateF(String id, Fare fare) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Fare not found",
                        "Cannot update non-existent fare with id " + id)))
                .flatMap(existingFare -> {
                    existingFare.setOrganizationId(fare.getOrganizationId());
                    existingFare.setFareName(fare.getFareName());
                    existingFare.setFareType(fare.getFareType());
                    existingFare.setFareAmount(fare.getFareAmount());
                    existingFare.setEffectiveDate(fare.getEffectiveDate()); // Update effective date
                    return fareRepository.save(existingFare);
                });
    }

    @Override
    public Mono<Void> deleteF(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Fare not found",
                        "Cannot delete non-existent fare with id " + id)))
                .flatMap(fareRepository::delete);
    }

    @Override
    public Mono<Fare> activateF(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<Fare> deactivateF(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .flatMap(fare -> {
                    fare.setStatus(Constants.INACTIVE.name());
                    return fareRepository.save(fare);
                });
    }

    private Mono<Fare> changeStatus(String id, String status) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("Fare", id)))
                .flatMap(fare -> {
                    System.out.println("➡️ Estado actual: " + fare.getStatus());
                    fare.setStatus(status);
                    System.out.println("🔁 Estado nuevo: " + fare.getStatus());
                    return fareRepository.save(fare)
                            .doOnNext(saved -> System.out.println("✅ Guardado: " + saved.getStatus()));
                })
                .doOnError(e -> System.err.println("❌ Error al cambiar estado: " + e.getMessage()));
    }
    
    // New methods for enriched fare data
    
    @Override
    public Mono<EnrichedFareResponse> getEnrichedById(String id) {
        return fareRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Fare not found",
                        "The requested fare with id " + id + " was not found")))
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllEnriched() {
        return fareRepository.findAll()
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllActiveEnriched() {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedFareResponse> getAllInactiveEnriched() {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .map(this::toEnrichedResponse);
    }
    
    private EnrichedFareResponse toEnrichedResponse(Fare fare) {
        return EnrichedFareResponse.builder()
                .id(fare.getId())
                .organizationId(fare.getOrganizationId())
                .fareCode(fare.getFareCode())
                .fareName(fare.getFareName())
                .fareType(fare.getFareType())
                .fareAmount(fare.getFareAmount())
                .status(fare.getStatus())
                .createdAt(fare.getCreatedAt())
                .build();
    }
    
    /**
     * Gets the current active fare based on the effective date
     * @param organizationId the organization ID
     * @return the current active fare
     */
    public Mono<Fare> getCurrentActiveFare(String organizationId) {
        Instant now = Instant.now();
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getOrganizationId().equals(organizationId))
                .filter(fare -> fare.getEffectiveDate() == null || fare.getEffectiveDate().isBefore(now) || fare.getEffectiveDate().equals(now))
                .sort((f1, f2) -> f2.getEffectiveDate().compareTo(f1.getEffectiveDate())) // Sort by effective date descending
                .next(); // Get the most recent one
    }
}