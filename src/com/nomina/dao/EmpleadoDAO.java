package com.nomina.dao;

import com.nomina.model.Empleado;
import com.nomina.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    private final String archivo = "data/empleados.csv";

    public List<Empleado> listar() {
        List<Empleado> empleados = new ArrayList<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] f = filas.get(i);
            boolean formatoConJefe = f.length >= 13;
            boolean formatoNuevo = f.length >= 12;
            Empleado e = new Empleado(
                    valor(f, 0), valor(f, 1), valor(f, 2), valor(f, 3), valor(f, 4),
                    formatoNuevo ? valor(f, 5) : "",
                    formatoConJefe ? valor(f, 6) : "",
                    formatoConJefe ? valor(f, 7) : (formatoNuevo ? valor(f, 6) : valor(f, 5)),
                    formatoConJefe ? valor(f, 8) : (formatoNuevo ? valor(f, 7) : valor(f, 6)),
                    formatoConJefe ? valor(f, 9) : (formatoNuevo ? valor(f, 8) : valor(f, 7)),
                    parseDouble(formatoConJefe ? valor(f, 10) : (formatoNuevo ? valor(f, 9) : valor(f, 8))),
                    formatoConJefe ? valor(f, 11) : (formatoNuevo ? valor(f, 10) : valor(f, 9)),
                    formatoConJefe ? valor(f, 12) : (formatoNuevo ? valor(f, 11) : valor(f, 10))
            );
            empleados.add(e);
        }
        return empleados;
    }

    public boolean agregar(Empleado empleado) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(empleado.getId())) {
                return false;
            }
        }

        filas.add(toFila(empleado));
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    public boolean eliminar(String idEmpleado) {
        List<String[]> filas = CsvUtil.leer(archivo);
        boolean eliminado = false;

        for (int i = filas.size() - 1; i >= 1; i--) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(idEmpleado)) {
                filas.remove(i);
                eliminado = true;
            }
        }

        if (eliminado) {
            CsvUtil.escribir(archivo, filas);
        }

        return eliminado;
    }

    private String[] toFila(Empleado empleado) {
        return new String[]{
                empleado.getId(),
                empleado.getNombre(),
                empleado.getRfc(),
                empleado.getCurp(),
                empleado.getNss(),
                empleado.getCorreo(),
                empleado.getJefeId(),
                empleado.getPuesto(),
                empleado.getPlaza(),
                empleado.getDepto(),
                String.valueOf(empleado.getSueldo()),
                empleado.getFechaAlta(),
                empleado.getEstatus()
        };
    }

    private String valor(String[] fila, int indice) {
        return indice < fila.length ? fila[indice] : "";
    }

    private double parseDouble(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(valor);
    }
}
