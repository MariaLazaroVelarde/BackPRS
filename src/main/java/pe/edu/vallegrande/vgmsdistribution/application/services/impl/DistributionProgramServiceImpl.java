package pe.edu.vallegrande.vgmsdistribution.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedDistributionProgramResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.repository.DistributionProgramRepository;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.adapter.out.UserAuthClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionProgramServiceImpl implements DistributionProgramService {

    private final DistributionProgramRepository repository;
    private final UserAuthClient userAuthClient;

    @Override
    public Flux<DistributionProgramResponse> getAll() {
        return repository.findAll()
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> getById(String id) {
        return repository.findById(id)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request) {
        DistributionProgram program = DistributionProgram.builder()
                .organizationId(request.getOrganizationId())
                .programCode(request.getProgramCode())
                .scheduleId(request.getScheduleId())
                .routeId(request.getRouteId())
                .zoneId(request.getZoneId())
                .streetId(request.getStreetId())
                .programDate(request.getProgramDate())
                .plannedStartTime(request.getPlannedStartTime())
                .plannedEndTime(request.getPlannedEndTime())
                .responsibleUserId(request.getResponsibleUserId())
                .observations(request.getObservations())
                .status("PLANNED")
                .createdAt(Instant.now())
                .build();

        return repository.save(program)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setOrganizationId(request.getOrganizationId());
                    existing.setProgramCode(request.getProgramCode());
                    existing.setScheduleId(request.getScheduleId());
                    existing.setRouteId(request.getRouteId());
                    existing.setZoneId(request.getZoneId());
                    existing.setStreetId(request.getStreetId());
                    existing.setProgramDate(request.getProgramDate());
                    existing.setPlannedStartTime(request.getPlannedStartTime());
                    existing.setPlannedEndTime(request.getPlannedEndTime());
                    existing.setResponsibleUserId(request.getResponsibleUserId());
                    existing.setObservations(request.getObservations());
                    
                    return repository.save(existing);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<DistributionProgramResponse> activate(String id) {
        return repository.findById(id)
                .flatMap(program -> {
                    program.setStatus("ACTIVE");
                    return repository.save(program);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> desactivate(String id) {
        return repository.findById(id)
                .flatMap(program -> {
                    program.setStatus("INACTIVE");
                    return repository.save(program);
                })
                .map(this::toResponse);
    }

    private DistributionProgramResponse toResponse(DistributionProgram program) {
        return DistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programDate(program.getProgramDate())
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt())
                .build();
    }
    
    // New methods for enriched distribution program data
    
    @Override
    public Mono<EnrichedDistributionProgramResponse> getEnrichedById(String id) {
        return repository.findById(id)
                .flatMap(this::toEnrichedResponse);
    }
    
    @Override
    public Flux<EnrichedDistributionProgramResponse> getAllEnriched() {
        return repository.findAll()
                .flatMap(this::toEnrichedResponse);
    }
    
    @Override
    public Mono<EnrichedDistributionProgramResponse> saveAndEnrich(DistributionProgramCreateRequest request) {
        DistributionProgram program = DistributionProgram.builder()
                .organizationId(request.getOrganizationId())
                .programCode(request.getProgramCode())
                .scheduleId(request.getScheduleId())
                .routeId(request.getRouteId())
                .zoneId(request.getZoneId())
                .streetId(request.getStreetId())
                .programDate(request.getProgramDate())
                .plannedStartTime(request.getPlannedStartTime())
                .plannedEndTime(request.getPlannedEndTime())
                .responsibleUserId(request.getResponsibleUserId())
                .observations(request.getObservations())
                .status("PLANNED")
                .createdAt(Instant.now())
                .build();

        return repository.save(program)
                .flatMap(this::toEnrichedResponse);
    }
    
    private Mono<EnrichedDistributionProgramResponse> toEnrichedResponse(DistributionProgram program) {
        EnrichedDistributionProgramResponse.EnrichedDistributionProgramResponseBuilder builder = EnrichedDistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programDate(program.getProgramDate())
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt());
        
        // Fetch organization details if organizationId is present
        if (program.getOrganizationId() != null && !program.getOrganizationId().isEmpty()) {
            return userAuthClient.getOrganizationById(program.getOrganizationId())
                    .map(orgResponse -> {
                        builder.organization(orgResponse);
                        return builder.build();
                    })
                    .onErrorResume(e -> {
                        log.warn("Failed to fetch organization details for ID {}: {}", program.getOrganizationId(), e.getMessage());
                        // Return response with basic organization info
                        OrganizationResponse orgResponse = OrganizationResponse.builder()
                                .organizationId(program.getOrganizationId())
                                .build();
                        builder.organization(orgResponse);
                        return Mono.just(builder.build());
                    })
                    .switchIfEmpty(Mono.fromSupplier(() -> {
                        // Return response with basic organization info if no organization found
                        OrganizationResponse orgResponse = OrganizationResponse.builder()
                                .organizationId(program.getOrganizationId())
                                .build();
                        builder.organization(orgResponse);
                        return builder.build();
                    }));
        }
        
        return Mono.just(builder.build());
    }
}