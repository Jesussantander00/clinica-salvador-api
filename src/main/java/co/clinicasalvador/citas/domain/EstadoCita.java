package co.clinicasalvador.citas.domain;

/**
 * Ciclo de vida de una cita (RN-06):
 * PROGRAMADA → ATENDIDA, o PROGRAMADA → CANCELADA. No hay otras transiciones.
 */
public enum EstadoCita {
    PROGRAMADA,
    ATENDIDA,
    CANCELADA
}
