package pe.edu.vallegrande.vgmsdistribution.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
/**
 * Configuraciónn de OpenAPI/Swagger para el microservicio de distribuciónn
 */

@Configuration
public class SwaggerConfig {

    @Bean
     public OpenAPI customOpenAPI() {
          return new OpenAPI()
                    .info(new Info()
                              .title("VG Microservicio de Distribución")
                              .description("API para gestión de distribución del sistema JASS Digital. " +
                                        "Disponible en: https://lab.vallegrande.edu.pe/jass/ms-distribution/")
                              .version("2.0.0")
                              .contact(new Contact()
                                        .name("Valle Grande")
                                        .email("soporte@vallegrande.edu.pe")
                                        .url("https://vallegrande.edu.pe"))
                              .license(new License()
                                        .name("MIT License")
                                        .url("https://opensource.org/licenses/MIT")));
     }

}