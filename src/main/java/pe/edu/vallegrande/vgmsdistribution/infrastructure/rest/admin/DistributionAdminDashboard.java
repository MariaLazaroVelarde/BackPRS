package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.*;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Distribution Admin Dashboard", description = "Unified admin dashboard for distribution management and MS-Users integration")
@RequiredArgsConstructor
@Slf4j
public class DistributionAdminDashboard {

	// Distribution Services
	private final DistributionProgramService programService;
	private final DistributionRouteService routeService;
	private final DistributionScheduleService scheduleService;
	private final FareService fareService;
	
	// ===============================
	// DASHBOARD & STATISTICS
	// ===============================

	@GetMapping("/dashboard/stats")
	@Operation(summary = "Get comprehensive dashboard statistics")
	public Mono<ResponseDto<Map<String, Object>>> getDashboardStats() {
		log.debug("Fetching comprehensive dashboard statistics");

		return Mono.zip(
				programService.getAll().count(),
				routeService.getAll().count(),
				scheduleService.getAll().count(),
				fareService.getAllF().count()
		)
		.map(tuple -> {
			Map<String, Object> stats = Map.of(
					"totalPrograms", tuple.getT1(),
					"totalRoutes", tuple.getT2(),
					"totalSchedules", tuple.getT3(),
					"totalFares", tuple.getT4(),
					"lastUpdated", LocalDateTime.now(),
					"systemStatus", "ACTIVE"
			);
			return new ResponseDto<>(true, stats);
		})
		.onErrorResume(e -> {
			log.error("Error fetching dashboard stats: {}", e.getMessage());
			return Mono.just(new ResponseDto<>(false,
					new ErrorMessage(500, "Error al obtener estadísticas del dashboard", e.getMessage())));
		});
	}

	@GetMapping("/dashboard/summary")
	@Operation(summary = "Get distribution system summary")
	public Mono<ResponseDto<Map<String, Object>>> getSystemSummary() {
		log.debug("Fetching system summary for admin dashboard");

		return Mono.zip(
				programService.getAll().collectList(),
				routeService.getAllActive().count(),
				scheduleService.getAllActive().count()
		)
		.map(tuple -> {
			var programs = tuple.getT1();
			long activePrograms = programs.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
			long plannedPrograms = programs.stream().filter(p -> "PLANNED".equals(p.getStatus())).count();
			
			Map<String, Object> summary = Map.of(
					"programs", Map.of(
							"total", programs.size(),
							"active", activePrograms,
							"planned", plannedPrograms
					),
					"infrastructure", Map.of(
							"activeRoutes", tuple.getT2(),
							"activeSchedules", tuple.getT3()
					),
					"timestamp", LocalDateTime.now()
			);
			return new ResponseDto<>(true, summary);
		})
		.onErrorResume(e -> {
			log.error("Error fetching system summary: {}", e.getMessage());
			return Mono.just(new ResponseDto<>(false,
					new ErrorMessage(500, "Error al obtener resumen del sistema", e.getMessage())));
		});
	}

	// ===============================
	// QUICK ACCESS TO DISTRIBUTION MODULES
	// ===============================

	@GetMapping("/quick/programs")
	@Operation(summary = "Quick access to latest programs")
	public Mono<ResponseDto<Map<String, Object>>> getQuickPrograms() {
		log.debug("Quick access: fetching latest programs");

		return programService.getAll()
				.take(10) // Latest 10 programs
				.collectList()
				.map(programs -> {
					Map<String, Object> result = Map.of(
							"programs", programs,
							"total", programs.size(),
							"note", "Showing latest 10 programs - Use /api/admin/programs for full list"
					);
					return new ResponseDto<>(true, result);
				})
				.onErrorResume(e -> {
					log.error("Error fetching quick programs: {}", e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener programas rápidos", e.getMessage())));
				});
	}

	@GetMapping("/quick/routes")
	@Operation(summary = "Quick access to active routes")
	public Mono<ResponseDto<Map<String, Object>>> getQuickRoutes() {
		log.debug("Quick access: fetching active routes");

		return routeService.getAllActive()
				.take(10) // Latest 10 routes
				.collectList()
				.map(routes -> {
					Map<String, Object> result = Map.of(
							"routes", routes,
							"total", routes.size(),
							"note", "Showing latest 10 active routes - Use /api/admin/routes for full list"
					);
					return new ResponseDto<>(true, result);
				})
				.onErrorResume(e -> {
					log.error("Error fetching quick routes: {}", e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener rutas rápidas", e.getMessage())));
				});
	}

	// ===============================
	// HEALTH & STATUS ENDPOINTS
	// ===============================

	@GetMapping("/health/distribution")
	@Operation(summary = "Check distribution system health status")
	public Mono<ResponseDto<Map<String, Object>>> getDistributionHealth() {
		log.debug("Checking distribution system health");

		return Mono.zip(
				programService.getAll().hasElements(),
				routeService.getAll().hasElements(),
				scheduleService.getAll().hasElements(),
				fareService.getAllF().hasElements()
		)
		.map(tuple -> {
			boolean allHealthy = tuple.getT1() && tuple.getT2() && tuple.getT3() && tuple.getT4();
			
			Map<String, Object> health = Map.of(
					"status", allHealthy ? "HEALTHY" : "NEEDS_SETUP",
					"components", Map.of(
							"programs", tuple.getT1() ? "OK" : "EMPTY",
							"routes", tuple.getT2() ? "OK" : "EMPTY",
							"schedules", tuple.getT3() ? "OK" : "EMPTY",
							"fares", tuple.getT4() ? "OK" : "EMPTY"
					),
					"timestamp", LocalDateTime.now()
			);
			return new ResponseDto<>(true, health);
		})
		.onErrorResume(e -> {
			log.error("Error checking distribution health: {}", e.getMessage());
			Map<String, Object> errorHealth = Map.of(
					"status", "ERROR",
					"error", e.getMessage(),
					"timestamp", LocalDateTime.now()
			);
			return Mono.just(new ResponseDto<>(false, errorHealth));
		});
	}

	@GetMapping("/health/ms-users")
	@Operation(summary = "Check MS-Users connectivity")
	public Mono<ResponseDto<Map<String, Object>>> getMsUsersHealth() {
		log.debug("Checking MS-Users connectivity");

		// Return a placeholder response since MS-Users integration is not available
		Map<String, Object> health = Map.of(
				"status", "NOT_IMPLEMENTED",
				"service", "MS-Users",
				"timestamp", LocalDateTime.now(),
				"note", "MS-Users integration not available"
		);
		return Mono.just(new ResponseDto<>(true, health));
	}
}