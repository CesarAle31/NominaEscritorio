package com.nomina.dao;

import com.nomina.model.Percepcion;
import com.nomina.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class PercepcionDAO {
    private final String archivo = "data/percepciones.csv";

    public List<Percepcion> listar() {
        List<Percepcion> lista = new ArrayList<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] f = filas.get(i);
            if (f.length < 6) {
                continue;
            }
            boolean formatoNuevo = f.length >= 7;
            lista.add(new Percepcion(
                    valor(f, 0),
                    valor(f, 1),
                    valor(f, 2),
                    valor(f, 3),
                    Double.parseDouble(valor(f, 4)),
                    formatoNuevo ? valor(f, 5) : "",
                    formatoNuevo ? valor(f, 6) : valor(f, 5)
            ));
        }
        return lista;
    }

    public boolean agregar(Percepcion percepcion) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(percepcion.getClave())) {
                return false;
            }
        }

        filas.add(new String[]{
                percepcion.getClave(),
                percepcion.getConcepto(),
                percepcion.getTipo(),
                percepcion.getBase(),
                String.valueOf(percepcion.getValor()),
                percepcion.getFechaAlta(),
                percepcion.getEstatus()
        });
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    public boolean eliminar(String clavePercepcion) {
        if ("001".equalsIgnoreCase(clavePercepcion)) {
            return false;
        }

        List<String[]> filas = CsvUtil.leer(archivo);
        boolean eliminado = false;

        for (int i = filas.size() - 1; i >= 1; i--) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(clavePercepcion)) {
                filas.remove(i);
                eliminado = true;
            }
        }

        if (eliminado) {
            CsvUtil.escribir(archivo, filas);
        }

        return eliminado;
    }

    private String valor(String[] fila, int indice) {
        return indice < fila.length ? fila[indice] : "";
    }
}
