package pe.edu.vallegrande.vgmsdistribution.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for MS-Users microservice integration
 */
@Configuration
@ConfigurationProperties(prefix = "microservices.users")
@Data
public class MsUsersConfig {

    private String baseUrl = "https://lab.vallegrande.edu.pe/jass/ms-users";
    private Endpoints endpoints = new Endpoints();
    private Auth auth = new Auth();
    private Timeout timeout = new Timeout();

    @Data
    public static class Endpoints {
        private String admins = "/internal/organizations/{organizationId}/admins";
        private String users = "/internal/organizations/{organizationId}/users";
        private String clients = "/internal/organizations/{organizationId}/clients";
        private String userById = "/internal/users/{userId}";
        private String createAdmin = "/internal/organizations/{organizationId}/create-admin";
        private String validate = "/internal/users/validate";
        private String organizationById = "/internal/organizations/{organizationId}"; 
    }

    @Data
    public static class Auth {
        private boolean enabled = false;
        private String type = "Bearer";
        private String token = "";
    }

    @Data
    public static class Timeout {
        private int connection = 10000;
        private int read = 10000;
        private int write = 10000;
    }
}