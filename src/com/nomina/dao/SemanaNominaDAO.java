package com.nomina.dao;

import com.nomina.model.SemanaNomina;
import com.nomina.util.CsvUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SemanaNominaDAO {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private final String archivo = "data/semanas_nomina.csv";

    public List<Integer> listarAnios() {
        asegurarArchivo();

        Set<Integer> anios = new LinkedHashSet<>();
        for (String[] fila : CsvUtil.leer(archivo)) {
            if (fila.length < 1 || "anio".equalsIgnoreCase(limpiar(fila[0]))) {
                continue;
            }
            anios.add(parseInt(limpiar(fila[0])));
        }

        return new ArrayList<>(anios);
    }

    public List<SemanaNomina> listarPorAnio(int anio) {
        asegurarAnio(anio);

        List<SemanaNomina> semanas = new ArrayList<>();
        for (String[] fila : CsvUtil.leer(archivo)) {
            if (fila.length < 4 || "anio".equalsIgnoreCase(limpiar(fila[0]))) {
                continue;
            }

            int anioFila = parseInt(limpiar(fila[0]));
            if (anioFila == anio) {
                semanas.add(new SemanaNomina(
                        anioFila,
                        parseInt(limpiar(fila[1])),
                        limpiar(fila[2]),
                        limpiar(fila[3])
                ));
            }
        }

        return semanas;
    }

    private void asegurarArchivo() {
        File file = new File(archivo);
        if (file.exists()) {
            return;
        }

        int anioActual = Year.now().getValue();
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"anio", "semana", "fechaInicio", "fechaFin"});
        filas.addAll(generarSemanas(anioActual - 1));
        filas.addAll(generarSemanas(anioActual));
        filas.addAll(generarSemanas(anioActual + 1));
        CsvUtil.escribir(archivo, filas);
    }

    private void asegurarAnio(int anio) {
        asegurarArchivo();

        List<String[]> filas = CsvUtil.leer(archivo);
        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            if (fila.length > 0 && parseInt(limpiar(fila[0])) == anio) {
                return;
            }
        }

        filas.addAll(generarSemanas(anio));
        CsvUtil.escribir(archivo, filas);
    }

    private List<String[]> generarSemanas(int anio) {
        List<String[]> semanas = new ArrayList<>();
        LocalDate fechaInicio = LocalDate.of(anio, 1, 1);

        for (int semana = 1; semana <= 52; semana++) {
            LocalDate inicioSemana = fechaInicio.plusWeeks(semana - 1L);
            LocalDate finSemana = inicioSemana.plusDays(6);
            semanas.add(new String[]{
                    String.valueOf(anio),
                    String.valueOf(semana),
                    inicioSemana.format(FORMATO_FECHA),
                    finSemana.format(FORMATO_FECHA)
            });
        }

        return semanas;
    }

    private int parseInt(String valor) {
        return Integer.parseInt(valor.trim());
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.replace("\uFEFF", "").trim();
    }
}
