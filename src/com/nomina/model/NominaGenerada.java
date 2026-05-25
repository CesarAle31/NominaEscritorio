package com.nomina.model;

import java.util.List;

public class NominaGenerada {
    private Empleado empleado;
    private String periodo;
    private String fechaInicio;
    private String fechaFin;
    private List<NominaDetalle> percepciones;
    private List<NominaDetalle> deducciones;
    private double totalPercepciones;
    private double totalDeducciones;
    private double subtotal;
    private double totalDescuentos;
    private double totalRetenciones;
    private double neto;

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public List<NominaDetalle> getPercepciones() { return percepciones; }
    public void setPercepciones(List<NominaDetalle> percepciones) { this.percepciones = percepciones; }

    public List<NominaDetalle> getDeducciones() { return deducciones; }
    public void setDeducciones(List<NominaDetalle> deducciones) { this.deducciones = deducciones; }

    public double getTotalPercepciones() { return totalPercepciones; }
    public void setTotalPercepciones(double totalPercepciones) { this.totalPercepciones = totalPercepciones; }

    public double getTotalDeducciones() { return totalDeducciones; }
    public void setTotalDeducciones(double totalDeducciones) { this.totalDeducciones = totalDeducciones; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(double totalDescuentos) { this.totalDescuentos = totalDescuentos; }

    public double getTotalRetenciones() { return totalRetenciones; }
    public void setTotalRetenciones(double totalRetenciones) { this.totalRetenciones = totalRetenciones; }

    public double getNeto() { return neto; }
    public void setNeto(double neto) { this.neto = neto; }
}
