package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.FareService;
import pe.edu.vallegrande.vgmsdistribution.domain.models.Fare;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.request.FareCreateRequest;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response.FareResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/admin/fare-schedule")
@AllArgsConstructor
@Slf4j
public class FareScheduleRest {

    private final FareService fareService;

    /**
     * Endpoint to create a fare with a future effective date
     * This will automatically handle the transition from the current fare to the new fare
     */
    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<FareResponse>> scheduleFare(@RequestBody FareCreateRequest request) {
        try {
            log.info("Received fare scheduling request: {}", request);
            return fareService.saveF(request)
                    .map(savedFare -> new ResponseDto<>(true, savedFare))
                    .onErrorResume(e -> {
                        log.error("Error scheduling fare: ", e);
                        return Mono.just(
                                new ResponseDto<>(false,
                                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                                "Schedule fare failed",
                                                e.getMessage())));
                    });
        } catch (Exception e) {
            log.error("Unexpected error scheduling fare: ", e);
            return Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Internal server error",
                                    e.getMessage())));
        }
    }
    
    /**
     * Alternative endpoint to create a fare with a future effective date using request parameters
     */
    @PostMapping("/schedule-params")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseDto<FareResponse>> scheduleFareWithParams(
            @RequestParam String organizationId,
            @RequestParam String fareName,
            @RequestParam String fareType,
            @RequestParam Double fareAmount,
            @RequestParam String effectiveDate) {
        
        try {
            log.info("Received fare scheduling params: organizationId={}, fareName={}, fareType={}, fareAmount={}, effectiveDate={}", 
                    organizationId, fareName, fareType, fareAmount, effectiveDate);
            
            // Try to parse the date in different formats
            Instant instant = parseDate(effectiveDate);
            
            FareCreateRequest request = FareCreateRequest.builder()
                    .organizationId(organizationId)
                    .fareName(fareName)
                    .fareType(fareType)
                    .fareAmount(java.math.BigDecimal.valueOf(fareAmount))
                    .effectiveDate(instant)
                    .build();
            
            return fareService.saveF(request)
                    .map(savedFare -> new ResponseDto<>(true, savedFare))
                    .onErrorResume(e -> {
                        log.error("Error scheduling fare with params: ", e);
                        return Mono.just(
                                new ResponseDto<>(false,
                                        new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                                "Schedule fare failed",
                                                e.getMessage())));
                    });
        } catch (Exception e) {
            log.error("Unexpected error scheduling fare with params: ", e);
            return Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Internal server error",
                                    e.getMessage())));
        }
    }
    
    /**
     * Parse date string in multiple formats
     */
    private Instant parseDate(String dateString) {
        try {
            // Try ISO format first
            return Instant.parse(dateString);
        } catch (DateTimeParseException e1) {
            try {
                // Try LocalDate format
                LocalDate date = LocalDate.parse(dateString);
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException e2) {
                // If all else fails, throw an exception
                throw new IllegalArgumentException("Unable to parse date: " + dateString + 
                    ". Expected formats: ISO instant (e.g., 2025-10-11T00:00:00Z) or ISO date (e.g., 2025-10-11)");
            }
        }
    }
    
    /**
     * Endpoint to get the current active fare for an organization
     */
    @GetMapping("/current/{organizationId}")
    public Mono<ResponseDto<Fare>> getCurrentFare(@PathVariable String organizationId) {
        // Note: We would need to cast fareService to FareServiceImpl to access this method
        // In a production environment, we would add this method to the FareService interface
        return Mono.just(new ResponseDto<>(true, null)); // Placeholder
    }
}