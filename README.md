# Clínica Salvador – API REST de citas médicas

Servicio web REST para agendar, consultar, cancelar y atender citas médicas en la **Clínica Salvador** (Cartagena de Indias). Está construido con **Java 17 y Spring Boot 3**, guarda la información en **PostgreSQL** y se despliega con **Docker**.

> Trabajo Colaborativo – Ingeniería de Software, Universidad de Cartagena.
> Autor: Jesús Daniel Quintana Santander.

---

## 1. Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation, Actuator) |
| Base de datos | PostgreSQL 16 + migraciones con Flyway |
| Documentación de la API | OpenAPI 3 / Swagger UI (springdoc) |
| Pruebas | JUnit 5, Mockito, AssertJ, MockMvc |
| Pruebas funcionales | Postman (colección incluida en `postman/`) |
| Despliegue | Docker (imagen multi-etapa) y Docker Compose |
| Gestión | Scrum, Git y GitHub |

## 2. Arquitectura

Arquitectura en capas con separación de responsabilidades:

```
cliente (Postman / app web)
      │  HTTP + JSON
      ▼
controller   → recibe la petición, valida el JSON (@Valid) y responde con el código HTTP correcto
      │
service      → casos de uso (interfaces + implementaciones)
  └─ regla   → reglas de agendamiento, una clase por regla (patrón Estrategia)
      │
repository   → Spring Data JPA (consultas derivadas y Specifications)
      │
domain       → entidades JPA con comportamiento (Cita.cancelar(), Medico.atiendeA() …)
      ▼
PostgreSQL   → tablas creadas por Flyway (db/migration)
```

Paquetes en `src/main/java/co/clinicasalvador/citas`:

| Paquete | Contenido |
|---|---|
| `controller` | `EspecialidadController`, `MedicoController`, `PacienteController`, `CitaController` |
| `service` / `service.impl` | Interfaces de servicio y sus implementaciones |
| `service.regla` | `ReglaAgendamiento` y sus implementaciones ordenadas con `@Order` |
| `repository` | Repositorios JPA y `CitaEspecificaciones` (filtros opcionales) |
| `domain` | `Especialidad`, `Medico`, `Paciente`, `Cita`, `Agenda`, enumeraciones |
| `dto` / `mapper` | Records de entrada/salida y conversión entidad → DTO |
| `exception` | Excepciones de negocio y `ManejadorGlobalErrores` (formato de error único) |
| `config` | Reloj de la aplicación (`Clock`) y metadatos de OpenAPI |

## 3. Reglas de negocio

| Código | Regla | Respuesta si se incumple |
|---|---|---|
| RN-01 | Un médico no puede tener dos citas activas a la misma hora | 409 Conflict |
| RN-02 | Un paciente no puede tener dos citas activas a la misma hora | 409 Conflict |
| RN-03 | La cita debe ser futura y empezar en un bloque de 30 minutos, dentro del horario del médico | 422 |
| RN-04 | Solo se agenda de lunes a viernes | 422 |
| RN-05 | El documento del paciente es único | 409 Conflict |
| RN-06 | Estados: PROGRAMADA → ATENDIDA o CANCELADA (no hay vuelta atrás) | 422 |
| RN-07 | Solo se cancela una cita programada y antes de su hora | 422 |

RN-01 y RN-02 también están protegidas en la base de datos con índices únicos parciales (`WHERE estado <> 'CANCELADA'`), de modo que dos reservas simultáneas nunca quedan registradas en el mismo horario y un horario cancelado vuelve a quedar libre.

## 4. Cómo ejecutar

### Opción A – Docker (recomendada)

Requisito: Docker Desktop.

```bash
docker compose up -d --build
```

* API: http://localhost:8080
* Swagger UI: http://localhost:8080/swagger-ui.html
* Salud: http://localhost:8080/actuator/health
* PostgreSQL del contenedor: `localhost:5433`, base `clinica_salvador`, usuario `clinica`, clave `clinica123`

Para detener: `docker compose down` (agregue `-v` para borrar también los datos).

### Opción B – Maven con un PostgreSQL local

1. Cree la base y el usuario:
   ```sql
   CREATE USER clinica WITH PASSWORD 'clinica123';
   CREATE DATABASE clinica_salvador OWNER clinica;
   ```
2. Ejecute `mvn spring-boot:run`. Flyway crea las tablas y carga los datos iniciales.

Variables de entorno opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`.

### Pruebas unitarias

```bash
mvn test
```

## 5. Endpoints

| Método | Ruta | Descripción | Códigos |
|---|---|---|---|
| GET | `/api/especialidades` | Lista las especialidades | 200 |
| POST | `/api/especialidades` | Crea una especialidad | 201, 400, 409 |
| GET | `/api/medicos?especialidadId=` | Lista médicos (filtro opcional) | 200 |
| GET | `/api/medicos/{id}` | Consulta un médico | 200, 404 |
| POST | `/api/medicos` | Registra un médico | 201, 400, 404, 409, 422 |
| PUT | `/api/medicos/{id}` | Actualiza datos y horario | 200, 400, 404, 409, 422 |
| PATCH | `/api/medicos/{id}/activar` · `/desactivar` | Cambia el estado del médico | 200, 404 |
| GET | `/api/medicos/{id}/disponibilidad?fecha=AAAA-MM-DD` | Horarios libres del día | 200, 404 |
| GET | `/api/pacientes` | Lista pacientes | 200 |
| GET | `/api/pacientes/{id}` | Consulta un paciente | 200, 404 |
| GET | `/api/pacientes/buscar?documento=` | Busca por documento | 200, 404 |
| POST | `/api/pacientes` | Registra un paciente | 201, 400, 409 |
| PUT | `/api/pacientes/{id}` | Actualiza datos de contacto | 200, 400, 404 |
| DELETE | `/api/pacientes/{id}` | Elimina un paciente sin citas | 204, 404, 409 |
| GET | `/api/citas?medicoId=&pacienteId=&fecha=&estado=` | Busca citas con filtros | 200, 400 |
| GET | `/api/citas/{id}` | Consulta una cita | 200, 404 |
| POST | `/api/citas` | Agenda una cita | 201, 400, 404, 409, 422 |
| PATCH | `/api/citas/{id}/cancelar` | Cancela una cita | 200, 404, 422 |
| PATCH | `/api/citas/{id}/atender` | Marca una cita como atendida | 200, 404, 422 |

### Ejemplo: agendar una cita

```http
POST /api/citas
Content-Type: application/json

{
  "pacienteId": 1,
  "medicoId": 1,
  "fechaHora": "2026-12-02T09:00:00",
  "motivo": "Control general"
}
```

Respuesta `201 Created` (encabezado `Location: /api/citas/1`):

```json
{
  "id": 1,
  "paciente": { "id": 1, "nombreCompleto": "Valentina Ruiz Mendoza", "documento": "1047383921" },
  "medico": { "id": 1, "nombreCompleto": "Andrés Martínez Pérez", "especialidad": "Medicina general" },
  "fechaHora": "2026-12-02T09:00:00",
  "motivo": "Control general",
  "estado": "PROGRAMADA",
  "creadaEn": "2026-11-20T11:30:12"
}
```

### Formato de error

Todas las respuestas de error tienen la misma forma:

```json
{
  "fecha": "2026-11-20T11:31:02",
  "estado": 422,
  "error": "Unprocessable Entity",
  "mensaje": "La clínica solo agenda citas de lunes a viernes.",
  "ruta": "/api/citas",
  "detalles": []
}
```

## 6. Pruebas con Postman

1. Importe `postman/clinica-salvador.postman_collection.json` y `postman/local.postman_environment.json`.
2. Seleccione el entorno **Local (Docker)**.
3. Ejecute la colección completa con *Run collection*. Cada solicitud trae sus pruebas automáticas (`pm.test`) y la colección calcula sola una fecha hábil futura, por lo que puede ejecutarse varias veces.

También se puede ejecutar por consola: `npx newman run postman/clinica-salvador.postman_collection.json -e postman/local.postman_environment.json`.

## 7. Datos iniciales

| Médico | Especialidad | Horario |
|---|---|---|
| Andrés Martínez Pérez | Medicina general | 08:00 – 12:00 |
| Carolina Herrera Díaz | Pediatría | 14:00 – 18:00 |
| Luis Gómez Arrieta | Ginecología | 08:00 – 16:00 |
| María Castro Julio | Odontología | 07:00 – 11:00 |

Pacientes de ejemplo: Valentina Ruiz Mendoza, Jorge Pérez Olivares y Sofía Barrios Cárdenas.
