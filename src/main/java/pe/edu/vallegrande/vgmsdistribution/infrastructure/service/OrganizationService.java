package pe.edu.vallegrande.vgmsdistribution.infrastructure.service;

import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.OrganizationRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.OrganizationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class OrganizationService {
    public abstract Mono<OrganizationResponse> create(OrganizationRequest request);

    public abstract Flux<OrganizationResponse> findAll();

    public abstract Mono<OrganizationResponse> findById(String id);

    public abstract Mono<OrganizationResponse> update(String id, OrganizationRequest request);

    public abstract Mono<Void> delete(String id);

    public abstract Mono<Void> restore(String id);
}
