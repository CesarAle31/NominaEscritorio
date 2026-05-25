package com.nomina.dao;

import com.nomina.util.CsvUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PeriodicidadDAO {
    private final String archivo = "data/periodicidades.csv";

    public List<String> listar() {
        asegurarArchivo();

        Set<String> periodicidades = new LinkedHashSet<>();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length == 0) {
                continue;
            }

            String periodicidad = fila[0].trim();
            if (!periodicidad.isEmpty()) {
                periodicidades.add(periodicidad);
            }
        }

        return new ArrayList<>(periodicidades);
    }

    public boolean agregar(String periodicidad) {
        String valor = periodicidad == null ? "" : periodicidad.trim();
        if (valor.isEmpty()) {
            return false;
        }

        asegurarArchivo();
        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && fila[0].trim().equalsIgnoreCase(valor)) {
                return false;
            }
        }

        filas.add(new String[]{valor});
        CsvUtil.escribir(archivo, filas);
        return true;
    }

    private void asegurarArchivo() {
        File file = new File(archivo);
        if (file.exists()) {
            return;
        }

        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"periodicidad"});
        filas.add(new String[]{"Semanal"});
        filas.add(new String[]{"Catorcenal"});
        filas.add(new String[]{"Quincenal"});
        filas.add(new String[]{"Mensual"});
        CsvUtil.escribir(archivo, filas);
    }
}
