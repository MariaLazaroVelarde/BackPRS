package pe.edu.vallegrande.vgmsdistribution.application.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Configuración de WebClient para comunicación con otros microservicios
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final MsUsersConfig msUsersConfig;

    @Bean("msUsersWebClient")
    public WebClient msUsersWebClient() {
        // Configuración del connection provider para optimizar conexiones
        ConnectionProvider connectionProvider = ConnectionProvider.builder("msUsers")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // Configuración del HttpClient con timeouts y redirecciones
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMillis(msUsersConfig.getTimeout().getConnection()))
                .followRedirect(true) // CRÍTICO: maneja redirecciones HTTP->HTTPS
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, msUsersConfig.getTimeout().getConnection());

        return WebClient.builder()
                .baseUrl(msUsersConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "vg-ms-distribution/1.0")
                .build();
    }

    /**
     * Configuration for MS-Users client properties
     */
    public MsUsersConfig getMsUsersConfig() {
        return msUsersConfig;
    }
}