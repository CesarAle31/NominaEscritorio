package com.nomina.dao;

import com.nomina.model.Deduccion;
import com.nomina.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class DeduccionDAO {
    private final String archivo = "data/deducciones.csv";

    public List<Deduccion> listar() {
        List<Deduccion> lista = new ArrayList<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] f = filas.get(i);
            if (f.length < 7) {
                continue;
            }
            boolean formatoNuevo = f.length >= 8;
            lista.add(new Deduccion(
                    valor(f, 0),
                    valor(f, 1),
                    valor(f, 2),
                    Double.parseDouble(valor(f, 3)),
                    Double.parseDouble(valor(f, 4)),
                    valor(f, 5),
                    formatoNuevo ? valor(f, 6) : "",
                    formatoNuevo ? valor(f, 7) : valor(f, 6)
            ));
        }
        return lista;
    }

    public boolean agregar(Deduccion deduccion) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(deduccion.getClave())) {
                return false;
            }
        }

        filas.add(toFila(deduccion));
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    public boolean actualizar(Deduccion deduccion) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(deduccion.getClave())) {
                filas.set(i, toFila(deduccion));
                CsvUtil.escribir(archivo, filas);
                return true;
            }
        }

        return false;
    }

    public boolean eliminar(String claveDeduccion) {
        List<String[]> filas = CsvUtil.leer(archivo);
        boolean eliminado = false;

        for (int i = filas.size() - 1; i >= 1; i--) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(claveDeduccion)) {
                filas.remove(i);
                eliminado = true;
            }
        }

        if (eliminado) {
            CsvUtil.escribir(archivo, filas);
        }

        return eliminado;
    }

    private String[] toFila(Deduccion deduccion) {
        return new String[]{
                deduccion.getClave(),
                deduccion.getConcepto(),
                deduccion.getTipo(),
                String.valueOf(deduccion.getValor()),
                String.valueOf(deduccion.getTope()),
                deduccion.getObligatoria(),
                deduccion.getFechaAlta(),
                deduccion.getEstatus()
        };
    }

    private String valor(String[] fila, int indice) {
        return indice < fila.length ? fila[indice] : "";
    }
}
