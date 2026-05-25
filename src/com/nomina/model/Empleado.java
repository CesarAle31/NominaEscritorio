package com.nomina.model;

public class Empleado {
    private String id;
    private String nombre;
    private String rfc;
    private String curp;
    private String nss;
    private String correo;
    private String jefeId;
    private String puesto;
    private String plaza;
    private String depto;
    private double sueldo;
    private String fechaAlta;
    private String estatus;

    public Empleado() {}

    public Empleado(String id, String nombre, String rfc, String curp, String nss, String correo,
                    String puesto, String plaza, String depto, double sueldo,
                    String fechaAlta, String estatus) {
        this(id, nombre, rfc, curp, nss, correo, "", puesto, plaza, depto, sueldo, fechaAlta, estatus);
    }

    public Empleado(String id, String nombre, String rfc, String curp, String nss, String correo,
                    String jefeId, String puesto, String plaza, String depto, double sueldo,
                    String fechaAlta, String estatus) {
        this.id = id;
        this.nombre = nombre;
        this.rfc = rfc;
        this.curp = curp;
        this.nss = nss;
        this.correo = correo;
        this.jefeId = jefeId;
        this.puesto = puesto;
        this.plaza = plaza;
        this.depto = depto;
        this.sueldo = sueldo;
        this.fechaAlta = fechaAlta;
        this.estatus = estatus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getNss() { return nss; }
    public void setNss(String nss) { this.nss = nss; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getJefeId() { return jefeId; }
    public void setJefeId(String jefeId) { this.jefeId = jefeId; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public String getPlaza() { return plaza; }
    public void setPlaza(String plaza) { this.plaza = plaza; }

    public String getDepto() { return depto; }
    public void setDepto(String depto) { this.depto = depto; }

    public double getSueldo() { return sueldo; }
    public void setSueldo(double sueldo) { this.sueldo = sueldo; }

    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
