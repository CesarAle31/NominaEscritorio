package com.nomina.dao;

import com.nomina.model.Plaza;
import com.nomina.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class PlazaDAO {
    private final String archivo = "data/plazas.csv";

    public List<Plaza> listar() {
        List<Plaza> plazas = new ArrayList<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] f = filas.get(i);
            if (f.length < 6) {
                continue;
            }
            boolean formatoNuevo = f.length >= 7;
            plazas.add(new Plaza(
                    valor(f, 0),
                    valor(f, 1),
                    valor(f, 2),
                    valor(f, 3),
                    Double.parseDouble(valor(f, 4)),
                    formatoNuevo ? valor(f, 5) : "",
                    formatoNuevo ? valor(f, 6) : valor(f, 5)
            ));
        }

        return plazas;
    }

    public Plaza buscarPorClave(String clave) {
        for (Plaza plaza : listar()) {
            if (plaza.getClave().equalsIgnoreCase(clave)) {
                return plaza;
            }
        }
        return null;
    }

    public boolean agregar(Plaza plaza) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(plaza.getClave())) {
                return false;
            }
        }

        filas.add(new String[]{
                plaza.getClave(),
                plaza.getNombre(),
                plaza.getDepto(),
                plaza.getConceptoPercepcion(),
                String.valueOf(plaza.getMontoPercepcion()),
                plaza.getFechaAlta(),
                plaza.getEstatus()
        });
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    public boolean eliminar(String clavePlaza) {
        List<String[]> filas = CsvUtil.leer(archivo);
        boolean eliminado = false;

        for (int i = filas.size() - 1; i >= 1; i--) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(clavePlaza)) {
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
