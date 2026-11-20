package co.clinicasalvador.citas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Expone un {@link Clock} como dependencia.
 *
 * <p>Las reglas que dependen de la fecha actual reciben el reloj por inyección,
 * de modo que las pruebas pueden fijar "ahora" a un instante conocido
 * (inversión de dependencias).</p>
 */
@Configuration
public class TiempoConfig {

    @Bean
    public Clock reloj(@Value("${agenda.zona-horaria:America/Bogota}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}
