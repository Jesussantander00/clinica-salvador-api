package co.clinicasalvador.citas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación interactiva (Swagger UI en /swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI().info(new Info()
                .title("Clínica Salvador - API de citas")
                .version("1.0.0")
                .description("Gestión de especialidades, médicos, pacientes y citas médicas. "
                        + "Todas las respuestas se entregan en formato JSON.")
                .contact(new Contact().name("Jesús Daniel Quintana Santander")));
    }
}
