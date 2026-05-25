package com.nomina.model;

public class Puesto {
    private String clave;
    private String nombre;
    private double sueldoBase;
    private String fechaAlta;
    private String estatus;

    public Puesto() {}

    public Puesto(String clave, String nombre, double sueldoBase, String estatus) {
        this(clave, nombre, sueldoBase, "", estatus);
    }

    public Puesto(String clave, String nombre, double sueldoBase, String fechaAlta, String estatus) {
        this.clave = clave;
        this.nombre = nombre;
        this.sueldoBase = sueldoBase;
        this.fechaAlta = fechaAlta;
        this.estatus = estatus;
    }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(double sueldoBase) { this.sueldoBase = sueldoBase; }

    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    @Override
    public String toString() {
        return clave + " - " + nombre;
    }
}
