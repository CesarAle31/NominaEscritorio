package com.nomina.service;

public class NumeroALetrasUtil {

    private static final String[] UNIDADES = {
            "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
            "diez", "once", "doce", "trece", "catorce", "quince", "dieciseis", "diecisiete",
            "dieciocho", "diecinueve", "veinte", "veintiuno", "veintidos", "veintitres",
            "veinticuatro", "veinticinco", "veintiseis", "veintisiete", "veintiocho", "veintinueve"
    };

    private static final String[] DECENAS = {
            "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos",
            "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    public static String convertirPesos(double monto) {
        long entero = (long) monto;
        int centavos = (int) Math.round((monto - entero) * 100);
        if (centavos == 100) {
            entero += 1;
            centavos = 0;
        }

        return convertir(entero) + " pesos " + String.format("%02d/100 M.N.", centavos);
    }

    private static String convertir(long numero) {
        if (numero == 0) return "cero";
        if (numero < 30) return UNIDADES[(int) numero];
        if (numero < 100) return convertirDecenas((int) numero);
        if (numero < 1000) return convertirCentenas((int) numero);
        if (numero < 1_000_000) return convertirMiles((int) numero);
        return convertirMillones(numero);
    }

    private static String convertirDecenas(int numero) {
        if (numero < 30) return UNIDADES[numero];
        int decena = numero / 10;
        int unidad = numero % 10;
        return unidad == 0 ? DECENAS[decena] : DECENAS[decena] + " y " + convertir(unidad);
    }

    private static String convertirCentenas(int numero) {
        if (numero == 100) return "cien";
        int centena = numero / 100;
        int resto = numero % 100;
        return resto == 0 ? CENTENAS[centena] : CENTENAS[centena] + " " + convertir(resto);
    }

    private static String convertirMiles(int numero) {
        int miles = numero / 1000;
        int resto = numero % 1000;
        String prefijo = miles == 1 ? "mil" : convertir(miles) + " mil";
        return resto == 0 ? prefijo : prefijo + " " + convertir(resto);
    }

    private static String convertirMillones(long numero) {
        long millones = numero / 1_000_000;
        long resto = numero % 1_000_000;
        String prefijo = millones == 1 ? "un millon" : convertir(millones) + " millones";
        return resto == 0 ? prefijo : prefijo + " " + convertir(resto);
    }
}
