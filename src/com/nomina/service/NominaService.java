package com.nomina.service;

import com.nomina.model.Deduccion;
import com.nomina.model.Empleado;
import com.nomina.model.NominaDetalle;
import com.nomina.model.NominaGenerada;
import com.nomina.model.Percepcion;
import com.nomina.model.Plaza;

import java.util.ArrayList;
import java.util.List;

public class NominaService {

    public NominaGenerada calcularNomina(Empleado empleado,
                                         Plaza plaza,
                                         List<Percepcion> percepcionesConfig,
                                         List<Deduccion> deduccionesConfig,
                                         String periodo,
                                         String fechaInicio,
                                         String fechaFin) {

        double sueldoBase = empleado.getSueldo() / 2.0;
        List<NominaDetalle> percepciones = new ArrayList<>();
        List<NominaDetalle> deducciones = new ArrayList<>();

        percepciones.add(new NominaDetalle("001", "Sueldo Base", sueldoBase));

        if (plaza != null
                && "Activo".equalsIgnoreCase(plaza.getEstatus())
                && plaza.getMontoPercepcion() > 0) {
            percepciones.add(new NominaDetalle(
                    "PLZ-" + plaza.getClave(),
                    plaza.getConceptoPercepcion(),
                    plaza.getMontoPercepcion()
            ));
        }

        for (Percepcion p : percepcionesConfig) {
            if (!"Activo".equalsIgnoreCase(p.getEstatus())) continue;
            if ("001".equals(p.getClave())) continue;

            double importe = 0;
            if ("% Sueldo".equalsIgnoreCase(p.getBase())) {
                importe = sueldoBase * (p.getValor() / 100.0);
            } else if ("Monto Fijo".equalsIgnoreCase(p.getBase())) {
                importe = p.getValor();
            }

            if (importe > 0) {
                percepciones.add(new NominaDetalle(p.getClave(), p.getConcepto(), importe));
            }
        }

        double totalPercepciones = percepciones.stream()
                .mapToDouble(NominaDetalle::getImporte)
                .sum();

        double totalDescuentos = 0;
        double totalRetenciones = 0;

        for (Deduccion d : deduccionesConfig) {
            if (!"Activo".equalsIgnoreCase(d.getEstatus())) continue;

            double importe = 0;
            if ("Porcentaje".equalsIgnoreCase(d.getTipo())) {
                importe = totalPercepciones * (d.getValor() / 100.0);
            } else if ("Monto Fijo".equalsIgnoreCase(d.getTipo())) {
                importe = d.getValor();
            }

            if (d.getTope() > 0 && importe > d.getTope()) {
                importe = d.getTope();
            }

            if (importe > 0) {
                deducciones.add(new NominaDetalle(d.getClave(), d.getConcepto(), importe));
                if ("Porcentaje".equalsIgnoreCase(d.getTipo())) {
                    totalRetenciones += importe;
                } else {
                    totalDescuentos += importe;
                }
            }
        }

        double totalDeducciones = deducciones.stream()
                .mapToDouble(NominaDetalle::getImporte)
                .sum();

        NominaGenerada nomina = new NominaGenerada();
        nomina.setEmpleado(empleado);
        nomina.setPeriodo(periodo);
        nomina.setFechaInicio(fechaInicio);
        nomina.setFechaFin(fechaFin);
        nomina.setPercepciones(percepciones);
        nomina.setDeducciones(deducciones);
        nomina.setTotalPercepciones(totalPercepciones);
        nomina.setTotalDeducciones(totalDeducciones);
        nomina.setSubtotal(totalPercepciones);
        nomina.setTotalDescuentos(totalDescuentos);
        nomina.setTotalRetenciones(totalRetenciones);
        nomina.setNeto(totalPercepciones - totalDeducciones);

        return nomina;
    }
}
