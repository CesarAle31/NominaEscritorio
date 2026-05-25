package com.nomina.dao;

import com.nomina.model.Puesto;
import com.nomina.util.CsvUtil;

import java.util.ArrayList;
import java.util.List;

public class PuestoDAO {
    private final String archivo = "data/puestos.csv";

    public List<Puesto> listar() {
        List<Puesto> puestos = new ArrayList<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] f = filas.get(i);
            if (f.length < 4) {
                continue;
            }
            boolean formatoNuevo = f.length >= 5;
            puestos.add(new Puesto(
                    valor(f, 0),
                    valor(f, 1),
                    Double.parseDouble(valor(f, 2)),
                    formatoNuevo ? valor(f, 3) : "",
                    formatoNuevo ? valor(f, 4) : valor(f, 3)
            ));
        }

        return puestos;
    }

    public boolean agregar(Puesto puesto) {
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(puesto.getClave())) {
                return false;
            }
        }

        filas.add(new String[]{
                puesto.getClave(),
                puesto.getNombre(),
                String.valueOf(puesto.getSueldoBase()),
                puesto.getFechaAlta(),
                puesto.getEstatus()
        });
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    public boolean eliminar(String clavePuesto) {
        List<String[]> filas = CsvUtil.leer(archivo);
        boolean eliminado = false;

        for (int i = filas.size() - 1; i >= 1; i--) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].equalsIgnoreCase(clavePuesto)) {
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
