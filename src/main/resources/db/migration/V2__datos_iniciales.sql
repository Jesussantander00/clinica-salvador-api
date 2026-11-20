-- Datos de ejemplo para probar la API (especialidades y médicos de la clínica).

INSERT INTO especialidad (nombre) VALUES
    ('Medicina general'),
    ('Pediatría'),
    ('Ginecología'),
    ('Odontología');

INSERT INTO medico (nombres, apellidos, registro_profesional, especialidad_id, hora_inicio, hora_fin, activo) VALUES
    ('Andrés', 'Martínez Pérez', 'RM-10234', 1, '08:00', '12:00', TRUE),
    ('Carolina', 'Herrera Díaz', 'RM-20987', 2, '14:00', '18:00', TRUE),
    ('Luis', 'Gómez Arrieta', 'RM-31456', 3, '08:00', '16:00', TRUE),
    ('María', 'Castro Julio', 'RM-47821', 4, '07:00', '11:00', TRUE);

INSERT INTO paciente (tipo_documento, documento, nombres, apellidos, telefono, correo, fecha_nacimiento) VALUES
    ('CC', '1047383921', 'Valentina', 'Ruiz Mendoza', '3014567890', 'valentina.ruiz@correo.com', '1995-04-12'),
    ('CC', '73151222', 'Jorge', 'Pérez Olivares', '3157894561', 'jorge.perez@correo.com', '1980-09-30'),
    ('TI', '1043990011', 'Sofía', 'Barrios Cárdenas', '3006543210', 'sofia.barrios@correo.com', '2012-01-25');
