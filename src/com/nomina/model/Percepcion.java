package com.nomina.model;

public class Percepcion {
    private String clave;
    private String concepto;
    private String tipo;
    private String base;
    private double valor;
    private String fechaAlta;
    private String estatus;

    public Percepcion() {}

    public Percepcion(String clave, String concepto, String tipo, String base, double valor, String estatus) {
        this(clave, concepto, tipo, base, valor, "", estatus);
    }

    public Percepcion(String clave, String concepto, String tipo, String base, double valor, String fechaAlta, String estatus) {
        this.clave = clave;
        this.concepto = concepto;
        this.tipo = tipo;
        this.base = base;
        this.valor = valor;
        this.fechaAlta = fechaAlta;
        this.estatus = estatus;
    }

    public String getClave() { return clave; }
    public String getConcepto() { return concepto; }
    public String getTipo() { return tipo; }
    public String getBase() { return base; }
    public double getValor() { return valor; }
    public String getFechaAlta() { return fechaAlta; }
    public String getEstatus() { return estatus; }
}
