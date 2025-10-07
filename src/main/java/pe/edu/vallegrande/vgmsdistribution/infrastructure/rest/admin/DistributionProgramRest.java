package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.EnrichedDistributionProgramResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/programs")
@RequiredArgsConstructor
public class DistributionProgramRest {

    private final DistributionProgramService programService;

    @GetMapping
    public Mono<ResponseDto<List<DistributionProgramResponse>>> getAll() {
        return programService.getAll()
                .collectList()
                .map(list -> new ResponseDto<>(true, list));
    }
    
    // New endpoint to get all enriched distribution programs
    @GetMapping("/enriched")
    public Mono<ResponseDto<List<EnrichedDistributionProgramResponse>>> getAllEnriched() {
        return programService.getAllEnriched()
                .collectList()
                .map(list -> new ResponseDto<>(true, list));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<DistributionProgramResponse>> getById(@PathVariable String id) {
        return programService.getById(id)
                .map(data -> new ResponseDto<>(true, data));
    }
    
    // New endpoint to get enriched distribution program by ID
    @GetMapping("/{id}/enriched")
    public Mono<ResponseDto<EnrichedDistributionProgramResponse>> getEnrichedById(@PathVariable String id) {
        return programService.getEnrichedById(id)
                .map(data -> new ResponseDto<>(true, data))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Distribution program not found", e.getMessage()))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<DistributionProgramResponse>> create(@RequestBody DistributionProgramCreateRequest request) {
        return programService.save(request)
                .map(data -> new ResponseDto<>(true, data));
    }
    
    // New endpoint to create a program and return enriched response
    @PostMapping("/enriched")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<EnrichedDistributionProgramResponse>> createAndEnrich(@RequestBody DistributionProgramCreateRequest request) {
        return programService.saveAndEnrich(request)
                .map(data -> new ResponseDto<>(true, data))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Failed to create distribution program", e.getMessage()))));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<DistributionProgramResponse>> update(@PathVariable String id, @RequestBody DistributionProgramCreateRequest request) {
        return programService.update(id, request)
                .map(data -> new ResponseDto<>(true, data));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseDto<Void>> delete(@PathVariable String id) {
        return programService.delete(id)
                .thenReturn(new ResponseDto<>(true, null));
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseDto<DistributionProgramResponse>> activate(@PathVariable String id) {
        return programService.activate(id)
                .map(programs -> new ResponseDto<>(true, programs))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Activation failed", e.getMessage()))));
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseDto<DistributionProgramResponse>> desactivate(@PathVariable String id) {
        return programService.desactivate(id)
                .map(programs -> new ResponseDto<>(true, programs))
                .onErrorResume(e -> Mono.just(new ResponseDto<>(false,
                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Deactivation failed", e.getMessage()))));
    }
}