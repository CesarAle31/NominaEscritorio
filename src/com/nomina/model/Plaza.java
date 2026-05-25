package com.nomina.model;

public class Plaza {
    private String clave;
    private String nombre;
    private String depto;
    private String conceptoPercepcion;
    private double montoPercepcion;
    private String fechaAlta;
    private String estatus;

    public Plaza() {}

    public Plaza(String clave, String nombre, String depto, String conceptoPercepcion, double montoPercepcion, String estatus) {
        this(clave, nombre, depto, conceptoPercepcion, montoPercepcion, "", estatus);
    }

    public Plaza(String clave, String nombre, String depto, String conceptoPercepcion,
                 double montoPercepcion, String fechaAlta, String estatus) {
        this.clave = clave;
        this.nombre = nombre;
        this.depto = depto;
        this.conceptoPercepcion = conceptoPercepcion;
        this.montoPercepcion = montoPercepcion;
        this.fechaAlta = fechaAlta;
        this.estatus = estatus;
    }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDepto() { return depto; }
    public void setDepto(String depto) { this.depto = depto; }

    public String getConceptoPercepcion() { return conceptoPercepcion; }
    public void setConceptoPercepcion(String conceptoPercepcion) { this.conceptoPercepcion = conceptoPercepcion; }

    public double getMontoPercepcion() { return montoPercepcion; }
    public void setMontoPercepcion(double montoPercepcion) { this.montoPercepcion = montoPercepcion; }

    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    @Override
    public String toString() {
        return clave + " - " + nombre;
    }
}
