package pe.edu.vallegrande.vgmsdistribution.application.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para el microservicio de organizaciones
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

     @Bean
     public OpenAPI customOpenAPI() {
          return new OpenAPI()
                    .info(new Info()
                              .title("VG Microservicio de distribution")
                              .description("API para gestión de distribution del sistema JASS Digital. La mayoría de los endpoints requieren un token JWT.")
                              .version("1.0.0")
                              .contact(new Contact()
                                        .name("Valle Grande")
                                        .email("soporte@vallegrande.edu.pe")));
     }
}