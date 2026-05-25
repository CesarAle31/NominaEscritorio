package com.nomina.model;

public class NominaDetalle {
    private String clave;
    private String concepto;
    private double importe;

    public NominaDetalle(String clave, String concepto, double importe) {
        this.clave = clave;
        this.concepto = concepto;
        this.importe = importe;
    }

    public String getClave() { return clave; }
    public String getConcepto() { return concepto; }
    public double getImporte() { return importe; }
}