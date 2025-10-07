package pe.edu.vallegrande.vgmsdistribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VgMsDistributionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VgMsDistributionApplication.class, args);
    }
}