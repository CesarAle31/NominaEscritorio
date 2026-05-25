CREATE TABLE departamentos (
    id_departamento VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activo'
);

CREATE TABLE puestos (
    clave VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    sueldo_base DECIMAL(12, 2) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activo'
);

CREATE TABLE plazas (
    clave VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_departamento VARCHAR(20) NOT NULL,
    concepto_percepcion VARCHAR(100),
    monto_percepcion DECIMAL(12, 2) DEFAULT 0,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activo',
    CONSTRAINT fk_plazas_departamento
        FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento)
);

CREATE TABLE empleados (
    id VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    rfc VARCHAR(13) NOT NULL,
    curp VARCHAR(18) NOT NULL,
    nss VARCHAR(11) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    jefe_id VARCHAR(20),
    puesto_clave VARCHAR(20) NOT NULL,
    plaza_clave VARCHAR(20) NOT NULL,
    sueldo DECIMAL(12, 2) NOT NULL,
    fecha_alta DATE NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activo',
    CONSTRAINT fk_empleados_jefe
        FOREIGN KEY (jefe_id) REFERENCES empleados(id),
    CONSTRAINT fk_empleados_puesto
        FOREIGN KEY (puesto_clave) REFERENCES puestos(clave),
    CONSTRAINT fk_empleados_plaza
        FOREIGN KEY (plaza_clave) REFERENCES plazas(clave),
    CONSTRAINT chk_empleado_no_es_su_jefe
        CHECK (jefe_id IS NULL OR jefe_id <> id)
);

CREATE INDEX idx_empleados_jefe_id ON empleados(jefe_id);
CREATE INDEX idx_empleados_plaza ON empleados(plaza_clave);
