package pe.edu.vallegrande.vgmsdistribution.infrastructure.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsdistribution.application.services.*;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.adapter.out.UserAuthClient;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers.MsUsersUserInfo;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
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
	
	// MS-Users Integration
	private final UserAuthClient userAuthClient;

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
	// MS-USERS INTEGRATION
	// ===============================

	@GetMapping("/organization/{organizationId}/users")
	@Operation(summary = "Get all users for organization from MS-Users")
	public Mono<ResponseDto<List<MsUsersUserInfo>>> getOrganizationUsers(@PathVariable String organizationId) {
		log.debug("Fetching users for organization: {}", organizationId);

		return userAuthClient.getUsersByOrganizationId(organizationId)
				.collectList()
				.map(users -> new ResponseDto<>(true, users))
				.onErrorResume(e -> {
					log.error("Error fetching users for organization {}: {}", organizationId, e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener usuarios de la organización", e.getMessage())));
				});
	}

	@GetMapping("/organization/{organizationId}/admins")
	@Operation(summary = "Get administrators for organization from MS-Users")
	public Mono<ResponseDto<List<MsUsersUserInfo>>> getOrganizationAdmins(@PathVariable String organizationId) {
		log.debug("Fetching admins for organization: {}", organizationId);

		return userAuthClient.getAdminsByOrganizationId(organizationId)
				.collectList()
				.map(admins -> new ResponseDto<>(true, admins))
				.onErrorResume(e -> {
					log.error("Error fetching admins for organization {}: {}", organizationId, e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener administradores", e.getMessage())));
				});
	}

	@GetMapping("/organization/{organizationId}/clients")
	@Operation(summary = "Get clients for organization from MS-Users")
	public Mono<ResponseDto<List<MsUsersUserInfo>>> getOrganizationClients(@PathVariable String organizationId) {
		log.debug("Fetching clients for organization: {}", organizationId);

		return userAuthClient.getClientsByOrganizationId(organizationId)
				.collectList()
				.map(clients -> new ResponseDto<>(true, clients))
				.onErrorResume(e -> {
					log.error("Error fetching clients for organization {}: {}", organizationId, e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener clientes", e.getMessage())));
				});
	}

	@GetMapping("/user/{userId}")
	@Operation(summary = "Get user details by ID from MS-Users")
	public Mono<ResponseDto<MsUsersUserInfo>> getUserDetails(@PathVariable String userId) {
		log.debug("Fetching user details: {}", userId);

		return userAuthClient.getUserById(userId)
				.map(user -> new ResponseDto<>(true, user))
				.switchIfEmpty(Mono.just(new ResponseDto<>(false,
						new ErrorMessage(404, "Usuario no encontrado", "User not found with ID: " + userId))))
				.onErrorResume(e -> {
					log.error("Error fetching user {}: {}", userId, e.getMessage());
					return Mono.just(new ResponseDto<>(false,
							new ErrorMessage(500, "Error al obtener detalles del usuario", e.getMessage())));
				});
	}

	@GetMapping("/organization/{organizationId}/user-summary")
	@Operation(summary = "Get user summary for organization")
	public Mono<ResponseDto<Map<String, Object>>> getOrganizationUserSummary(@PathVariable String organizationId) {
		log.debug("Fetching user summary for organization: {}", organizationId);

		return Mono.zip(
				userAuthClient.getUsersByOrganizationId(organizationId).count(),
				userAuthClient.getAdminsByOrganizationId(organizationId).count(),
				userAuthClient.getClientsByOrganizationId(organizationId).count()
		)
		.map(tuple -> {
			Map<String, Object> userSummary = Map.of(
					"organizationId", organizationId,
					"totalUsers", tuple.getT1(),
					"totalAdmins", tuple.getT2(),
					"totalClients", tuple.getT3(),
					"lastUpdated", LocalDateTime.now()
			);
			return new ResponseDto<>(true, userSummary);
		})
		.onErrorResume(e -> {
			log.error("Error fetching user summary for organization {}: {}", organizationId, e.getMessage());
			return Mono.just(new ResponseDto<>(false,
					new ErrorMessage(500, "Error al obtener resumen de usuarios", e.getMessage())));
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
			boolean allHealthy = tuple.getT1() || tuple.getT2() || tuple.getT3() || tuple.getT4();
			
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

		// Test connectivity by trying to get users for a test organization
		return userAuthClient.validateUserByEmail("test@example.com")
				.map(isValid -> {
					Map<String, Object> health = Map.of(
							"status", "CONNECTED",
							"service", "MS-Users",
							"timestamp", LocalDateTime.now(),
							"note", "Service is responding"
					);
					return new ResponseDto<>(true, health);
				})
				.onErrorResume(e -> {
					log.warn("MS-Users connectivity issue: {}", e.getMessage());
					Map<String, Object> health = Map.of(
							"status", "CONNECTION_ISSUE",
							"service", "MS-Users", 
							"error", e.getMessage(),
							"timestamp", LocalDateTime.now()
					);
					return Mono.just(new ResponseDto<>(false, health));
				});
	}
}