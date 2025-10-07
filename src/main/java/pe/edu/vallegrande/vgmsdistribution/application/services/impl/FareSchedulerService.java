package pe.edu.vallegrande.vgmsdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsdistribution.domain.models.Fare;
import pe.edu.vallegrande.vgmsdistribution.domain.enums.Constants;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.repository.FareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class FareSchedulerService {

    @Autowired
    private FareRepository fareRepository;

    /**
     * Scheduled task that runs every hour to check for fare transitions
     * This will activate/deactivate fares based on their effective dates
     */
    @Scheduled(cron = "0 0 * * * ?") // Run every hour
    public void processFareTransitions() {
        log.info("Processing fare transitions...");
        
        Instant now = Instant.now();
        
        // Handle fare activation: activate INACTIVE fares that have reached their effective date
        // Then handle fare deactivation
        activateScheduledFares(now)
                .then(deactivateExpiredFares(now))
                .subscribe(
                    unused -> log.info("Fare transition processing completed."),
                    error -> log.error("Error processing fare transitions: ", error)
                );
    }
    
    /**
     * Activate fares that have reached their effective date
     */
    private Mono<Void> activateScheduledFares(Instant now) {
        return fareRepository.findAllByStatus(Constants.INACTIVE.name())
                .filter(fare -> fare.getEffectiveDate() != null && 
                               (fare.getEffectiveDate().isBefore(now) || fare.getEffectiveDate().equals(now)))
                .flatMap(fare -> {
                    log.info("Activating fare {} for organization {} as of {}", 
                            fare.getFareCode(), fare.getOrganizationId(), fare.getEffectiveDate());
                    fare.setStatus(Constants.ACTIVE.name());
                    return fareRepository.save(fare)
                            .flatMap(this::handleFareActivation);
                })
                .then();
    }
    
    /**
     * Deactivate fares that have expired (newer active fare exists for same organization)
     */
    private Mono<Void> deactivateExpiredFares(Instant now) {
        return fareRepository.findAllByStatus(Constants.ACTIVE.name())
                .filter(fare -> fare.getEffectiveDate() != null && fare.getEffectiveDate().isBefore(now))
                .collectList()
                .flatMap(activeFares -> {
                    // For each organization, find the latest fare that should be active
                    return Flux.fromIterable(activeFares)
                            .groupBy(Fare::getOrganizationId)
                            .flatMap(group -> {
                                String organizationId = group.key();
                                return group.sort((f1, f2) -> f2.getEffectiveDate().compareTo(f1.getEffectiveDate()))
                                        .next() // Get the most recent fare for this organization
                                        .flatMap(latestFare -> {
                                            // Deactivate all other active fares for this organization
                                            return fareRepository.findByOrganizationIdAndStatusOrderByEffectiveDateDesc(
                                                            organizationId, Constants.ACTIVE.name())
                                                    .filter(fare -> !fare.getId().equals(latestFare.getId()))
                                                    .flatMap(fare -> {
                                                        fare.setStatus(Constants.INACTIVE.name());
                                                        log.info("Deactivating fare {} for organization {}", 
                                                                fare.getFareCode(), organizationId);
                                                        return fareRepository.save(fare);
                                                    })
                                                    .then();
                                        });
                            })
                            .then();
                });
    }
    
    /**
     * Handle fare activation by deactivating previous fares
     */
    private Mono<Fare> handleFareActivation(Fare activatedFare) {
        // Deactivate all other active fares for the same organization
        return fareRepository.findByOrganizationIdAndStatusOrderByEffectiveDateDesc(
                        activatedFare.getOrganizationId(), Constants.ACTIVE.name())
                .filter(fare -> !fare.getId().equals(activatedFare.getId()))
                .flatMap(fare -> {
                    fare.setStatus(Constants.INACTIVE.name());
                    log.info("Deactivating previous fare {} for organization {}", 
                            fare.getFareCode(), activatedFare.getOrganizationId());
                    return fareRepository.save(fare);
                })
                .then(Mono.just(activatedFare));
    }
}