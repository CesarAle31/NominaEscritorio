package com.nomina.model;

public class Deduccion {
    private String clave;
    private String concepto;
    private String tipo;
    private double valor;
    private double tope;
    private String obligatoria;
    private String fechaAlta;
    private String estatus;

    public Deduccion() {}

    public Deduccion(String clave, String concepto, String tipo, double valor, double tope,
                     String obligatoria, String estatus) {
        this(clave, concepto, tipo, valor, tope, obligatoria, "", estatus);
    }

    public Deduccion(String clave, String concepto, String tipo, double valor, double tope,
                     String obligatoria, String fechaAlta, String estatus) {
        this.clave = clave;
        this.concepto = concepto;
        this.tipo = tipo;
        this.valor = valor;
        this.tope = tope;
        this.obligatoria = obligatoria;
        this.fechaAlta = fechaAlta;
        this.estatus = estatus;
    }

    public String getClave() { return clave; }
    public String getConcepto() { return concepto; }
    public String getTipo() { return tipo; }
    public double getValor() { return valor; }
    public double getTope() { return tope; }
    public String getObligatoria() { return obligatoria; }
    public String getFechaAlta() { return fechaAlta; }
    public String getEstatus() { return estatus; }
}
