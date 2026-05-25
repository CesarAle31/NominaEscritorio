package com.nomina.model;

public class SemanaNomina {
    private int anio;
    private int numero;
    private String fechaInicio;
    private String fechaFin;

    public SemanaNomina(int anio, int numero, String fechaInicio, String fechaFin) {
        this.anio = anio;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public int getAnio() {
        return anio;
    }

    public int getNumero() {
        return numero;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    @Override
    public String toString() {
        return String.format("%02d", numero);
    }
}
