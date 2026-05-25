package com.nomina.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    public static List<String[]> leer(String ruta) {
        List<String[]> filas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                filas.add(linea.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filas;
    }

    public static void escribir(String ruta, List<String[]> filas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            for (String[] fila : filas) {
                bw.write(String.join(",", fila));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
