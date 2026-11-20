package co.clinicasalvador.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la API REST de citas de la Clínica Salvador. */
@SpringBootApplication
public class ClinicaSalvadorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaSalvadorApiApplication.class, args);
    }
}
