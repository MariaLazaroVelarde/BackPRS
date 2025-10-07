package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedDistributionRouteResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/routes")
@AllArgsConstructor
public class DistributionRouteRest {

    private final DistributionRouteService routeService;

    @GetMapping
    public Mono<ResponseDto<List<DistributionRoute>>> getAll() {
        return routeService.getAll()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }
    
    // New endpoint to get all enriched distribution routes
    @GetMapping("/enriched")
    public Mono<ResponseDto<List<EnrichedDistributionRouteResponse>>> getAllEnriched() {
        return routeService.getAllEnriched()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/active")
    public Mono<ResponseDto<List<DistributionRoute>>> getAllActive() {
        return routeService.getAllActive()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }
    
    // New endpoint to get all active enriched distribution routes
    @GetMapping("/active/enriched")
    public Mono<ResponseDto<List<EnrichedDistributionRouteResponse>>> getAllActiveEnriched() {
        return routeService.getAllActiveEnriched()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/inactive")
    public Mono<ResponseDto<List<DistributionRoute>>> getAllInactive() {
        return routeService.getAllInactive()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }
    
    // New endpoint to get all inactive enriched distribution routes
    @GetMapping("/inactive/enriched")
    public Mono<ResponseDto<List<EnrichedDistributionRouteResponse>>> getAllInactiveEnriched() {
        return routeService.getAllInactiveEnriched()
                .collectList()
                .map(routes -> new ResponseDto<>(true, routes));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<DistributionRoute>> getById(@PathVariable String id) {
        return routeService.getById(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Route not found", e.getMessage()))));
    }
    
    // New endpoint to get enriched distribution route by ID
    @GetMapping("/{id}/enriched")
    public Mono<ResponseDto<EnrichedDistributionRouteResponse>> getEnrichedById(@PathVariable String id) {
        return routeService.getEnrichedById(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Route not found", e.getMessage()))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<DistributionRouteResponse>> create(@RequestBody DistributionRouteCreateRequest request) {
        return routeService.save(request)
                .map(saved -> new ResponseDto<>(true, saved))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Validation error", e.getMessage()))));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<DistributionRoute>> update(@PathVariable String id, @RequestBody DistributionRoute route) {
        return routeService.update(id, route)
                .map(updated -> new ResponseDto<>(true, updated))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Update failed", e.getMessage()))));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseDto<Object>> delete(@PathVariable String id) {
        return routeService.delete(id)
                .then(Mono.just(new ResponseDto<>(true, null)))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Delete failed", e.getMessage()))));
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseDto<DistributionRoute>> activate(@PathVariable String id) {
        return routeService.activate(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Activation failed", e.getMessage()))));
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseDto<DistributionRoute>> deactivate(@PathVariable String id) {
        return routeService.deactivate(id)
                .map(route -> new ResponseDto<>(true, route))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Deactivation failed", e.getMessage()))));
    }
}