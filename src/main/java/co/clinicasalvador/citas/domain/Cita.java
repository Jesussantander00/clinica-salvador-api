package co.clinicasalvador.citas.domain;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Cita médica entre un paciente y un médico.
 *
 * <p>La propia entidad protege su ciclo de vida: solo una cita PROGRAMADA puede
 * cancelarse o marcarse como atendida (modelo de dominio rico).</p>
 */
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoCita estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    protected Cita() {
        // Requerido por JPA.
    }

    public Cita(Paciente paciente, Medico medico, LocalDateTime fechaHora, String motivo, LocalDateTime creadaEn) {
        this.paciente = paciente;
        this.medico = medico;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.creadaEn = creadaEn;
        this.estado = EstadoCita.PROGRAMADA;
    }

    /** Cancela la cita. Solo es posible si está programada y aún no ha llegado su hora (RN-07). */
    public void cancelar(LocalDateTime ahora) {
        exigirProgramada("cancelar");
        if (!fechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("No se puede cancelar una cita cuya hora ya pasó.");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    /** Registra que el paciente fue atendido. Solo es posible desde la hora de la cita. */
    public void marcarAtendida(LocalDateTime ahora) {
        exigirProgramada("marcar como atendida");
        if (ahora.isBefore(fechaHora)) {
            throw new ReglaNegocioException("No se puede marcar como atendida una cita que aún no ha comenzado.");
        }
        this.estado = EstadoCita.ATENDIDA;
    }

    private void exigirProgramada(String accion) {
        if (estado != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException(
                    "No se puede " + accion + " una cita en estado " + estado + ".");
        }
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}
