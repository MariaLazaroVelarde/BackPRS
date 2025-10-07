package pe.edu.vallegrande.vgmsdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.vgmsdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.vgmsdistribution.domain.enums.Constants;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedDistributionRouteResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.repository.DistributionRouteRepository;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.adapter.out.UserAuthClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class DistributionRouteServiceImpl implements DistributionRouteService {

    @Autowired
    private DistributionRouteRepository repository;
    
    @Autowired
    private UserAuthClient userAuthClient;

    @Override
    public Flux<DistributionRoute> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionRoute> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionRoute> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)));
    }

    @Override
    public Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request) {
        return generateNextRouteCode()
                .flatMap(generatedCode ->
                        repository.existsByRouteCode(generatedCode)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new CustomException(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Route code already exists",
                                                "Route code " + generatedCode + " already exists"));
                                    }

                                    DistributionRoute route = DistributionRoute.builder()
                                            .organizationId(request.getOrganizationId())
                                            .routeCode(generatedCode)
                                            .routeName(request.getRouteName())
                                            .zones(request.getZoneId())
                                            .totalEstimatedDuration(request.getTotalEstimatedDuration())
                                            .responsibleUserId(request.getResponsibleUserId())
                                            .status(Constants.ACTIVE.name())
                                            .createdAt(Instant.now())
                                            .build();

                                    return repository.save(route)
                                            .map(saved -> DistributionRouteResponse.builder()
                                                    .id(saved.getId())
                                                    .organizationId(saved.getOrganizationId())
                                                    .routeCode(saved.getRouteCode())
                                                    .routeName(saved.getRouteName())
                                                    .zoneId(saved.getZones())
                                                    .totalEstimatedDuration(saved.getTotalEstimatedDuration())
                                                    .responsibleUserId(saved.getResponsibleUserId())
                                                    .status(saved.getStatus())
                                                    .createdAt(saved.getCreatedAt())
                                                    .build());
                                })
                );
    }

    private static final String ROUTE_PREFIX = "RUT";

    private Mono<String> generateNextRouteCode() {
        return repository.findTopByOrderByRouteCodeDesc()
                .map(last -> {
                    String lastCode = last.getRouteCode();
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(ROUTE_PREFIX, ""));
                    } catch (NumberFormatException ignored) {}
                    return String.format(ROUTE_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(ROUTE_PREFIX + "001");
    }

    @Override
    public Mono<DistributionRoute> update(String id, DistributionRoute route) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)))
                .flatMap(existing -> {
                    existing.setRouteName(route.getRouteName());
                    existing.setZones(route.getZones());
                    existing.setTotalEstimatedDuration(route.getTotalEstimatedDuration());
                    existing.setResponsibleUserId(route.getResponsibleUserId());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "Cannot delete route with id " + id)))
                .flatMap(repository::delete);
    }

    @Override
    public Mono<DistributionRoute> activate(String id) {
        return changeStatus(id, Constants.ACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE.name());
    }

    private Mono<DistributionRoute> changeStatus(String id, String status) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "Cannot change status of route with id " + id)))
                .flatMap(route -> {
                    route.setStatus(status);
                    return repository.save(route);
                });
    }
    
    // New methods for enriched distribution route data
    
    @Override
    public Mono<EnrichedDistributionRouteResponse> getEnrichedById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "Route not found",
                        "No route found with id " + id)))
                .flatMap(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllEnriched() {
        return repository.findAll()
                .flatMap(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllActiveEnriched() {
        return repository.findAllByStatus(Constants.ACTIVE.name())
                .flatMap(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionRouteResponse> getAllInactiveEnriched() {
        return repository.findAllByStatus(Constants.INACTIVE.name())
                .flatMap(this::toEnrichedResponse);
    }
    
    private Mono<EnrichedDistributionRouteResponse> toEnrichedResponse(DistributionRoute route) {
        EnrichedDistributionRouteResponse.EnrichedDistributionRouteResponseBuilder builder = EnrichedDistributionRouteResponse.builder()
                .id(route.getId())
                .organizationId(route.getOrganizationId())
                .routeCode(route.getRouteCode())
                .routeName(route.getRouteName())
                .zoneId(route.getZones())
                .totalEstimatedDuration(route.getTotalEstimatedDuration())
                .responsibleUserId(route.getResponsibleUserId())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt());
        
        // Fetch organization details if organizationId is present
        if (route.getOrganizationId() != null && !route.getOrganizationId().isEmpty()) {
            return userAuthClient.getOrganizationById(route.getOrganizationId())
                    .map(orgResponse -> {
                        builder.organization(orgResponse);
                        return builder.build();
                    })
                    .onErrorResume(e -> {
                        log.warn("Failed to fetch organization details for ID {}: {}", route.getOrganizationId(), e.getMessage());
                        // Return response with basic organization info
                        OrganizationResponse orgResponse = OrganizationResponse.builder()
                                .organizationId(route.getOrganizationId())
                                .build();
                        builder.organization(orgResponse);
                        return Mono.just(builder.build());
                    })
                    .switchIfEmpty(Mono.fromSupplier(() -> {
                        // Return response with basic organization info if no organization found
                        OrganizationResponse orgResponse = OrganizationResponse.builder()
                                .organizationId(route.getOrganizationId())
                                .build();
                        builder.organization(orgResponse);
                        return builder.build();
                    }));
        }
        
        return Mono.just(builder.build());
    }
}