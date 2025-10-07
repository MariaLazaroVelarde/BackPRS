package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.FareService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.Fare;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedFareResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/fare")
@AllArgsConstructor
public class FareRest {

    private final FareService fareService;

    @GetMapping
    public Mono<ResponseDto<List<Fare>>> getAll() {
        return fareService.getAllF()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }
    
    // New endpoint to get all enriched fares
    @GetMapping("/enriched")
    public Mono<ResponseDto<List<EnrichedFareResponse>>> getAllEnriched() {
        return fareService.getAllEnriched()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }

    @GetMapping("/active")
    public Mono<ResponseDto<List<Fare>>> getAllActive() {
        return fareService.getAllActiveF()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }
    
    // New endpoint to get all active enriched fares
    @GetMapping("/active/enriched")
    public Mono<ResponseDto<List<EnrichedFareResponse>>> getAllActiveEnriched() {
        return fareService.getAllActiveEnriched()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }

    @GetMapping("/inactive")
    public Mono<ResponseDto<List<Fare>>> getAllInactive() {
        return fareService.getAllInactiveF()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }
    
    // New endpoint to get all inactive enriched fares
    @GetMapping("/inactive/enriched")
    public Mono<ResponseDto<List<EnrichedFareResponse>>> getAllInactiveEnriched() {
        return fareService.getAllInactiveEnriched()
                .collectList()
                .map(fares -> new ResponseDto<>(true, fares));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<Fare>> getById(@PathVariable String id) {
        return fareService.getByIdFMono(id)
                .map(fare -> new ResponseDto<>(true, fare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                                        "Fare not found",
                                        e.getMessage()))));
    }
    
    // New endpoint to get enriched fare by ID
    @GetMapping("/{id}/enriched")
    public Mono<ResponseDto<EnrichedFareResponse>> getEnrichedById(@PathVariable String id) {
        return fareService.getEnrichedById(id)
                .map(fare -> new ResponseDto<>(true, fare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                                        "Fare not found",
                                        e.getMessage()))));
    }
    
    // New endpoint to get current active fare for an organization
    @GetMapping("/current/{organizationId}")
    public Mono<ResponseDto<Fare>> getCurrentActiveFare(@PathVariable String organizationId) {
        return fareService.getCurrentActiveFare(organizationId)
                .map(fare -> new ResponseDto<>(true, fare))
                .defaultIfEmpty(new ResponseDto<>(true, null))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Failed to get current fare",
                                        e.getMessage()))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<FareResponse>> create(@RequestBody FareCreateRequest request) {
        return fareService.saveF(request)
                .map(savedFare -> new ResponseDto<>(true, savedFare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Validation error",
                                        e.getMessage()))));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<Fare>> update(@PathVariable String id, @RequestBody Fare fare) {
        return fareService.updateF(id, fare)
                .map(updatedFare -> new ResponseDto<>(true, updatedFare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Update failed",
                                        e.getMessage()))));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseDto<Object>> delete(@PathVariable String id) {
        return fareService.deleteF(id)
                .then(Mono.just(new ResponseDto<>(true, null)))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Delete failed",
                                        e.getMessage()))));
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseDto<Fare>> activate(@PathVariable String id) {
        return fareService.activateF(id)
                .map(fare -> new ResponseDto<>(true, fare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Activation failed",
                                        e.getMessage()))));
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseDto<Fare>> deactivate(@PathVariable String id) {
        return fareService.deactivateF(id)
                .map(fare -> new ResponseDto<>(true, fare))
                .onErrorResume(e -> Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Deactivation failed",
                                        e.getMessage()))));
    }
}