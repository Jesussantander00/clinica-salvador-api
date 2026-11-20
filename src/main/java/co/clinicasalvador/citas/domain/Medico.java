package co.clinicasalvador.citas.domain;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

/**
 * Médico de la clínica con su horario diario de atención.
 *
 * <p>El horario se guarda como hora de inicio y hora de fin; las citas se
 * agendan en bloques fijos dentro de ese rango (ver {@link Agenda}).</p>
 */
@Entity
@Table(name = "medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(name = "registro_profesional", nullable = false, unique = true, length = 30)
    private String registroProfesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especialidad_id")
    private Especialidad especialidad;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo = true;

    protected Medico() {
        // Requerido por JPA.
    }

    public Medico(String nombres, String apellidos, String registroProfesional,
                  Especialidad especialidad, LocalTime horaInicio, LocalTime horaFin) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.registroProfesional = registroProfesional;
        this.especialidad = especialidad;
        cambiarHorario(horaInicio, horaFin);
    }

    /** Cambia el horario de atención validando que la hora de fin sea posterior al inicio. */
    public final void cambiarHorario(LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("La hora de fin del horario debe ser posterior a la hora de inicio.");
        }
        this.horaInicio = inicio;
        this.horaFin = fin;
    }

    /** {@code true} si una cita que empieza a esa hora termina dentro del horario de atención. */
    public boolean atiendeA(LocalTime hora) {
        return !hora.isBefore(horaInicio) && !hora.plus(Agenda.DURACION_CITA).isAfter(horaFin);
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public Long getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getRegistroProfesional() {
        return registroProfesional;
    }

    public void setRegistroProfesional(String registroProfesional) {
        this.registroProfesional = registroProfesional;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
