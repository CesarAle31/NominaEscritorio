package com.nomina.util;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMPLOYEE_ID = Pattern.compile("(?i)^E-\\d{3}$");
    private static final Pattern PERSON_NAME = Pattern.compile("^[\\p{L}][\\p{L} .'-]{2,99}$");
    private static final Pattern RFC = Pattern.compile("^[A-Z]{3,4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern CURP = Pattern.compile("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$");
    private static final Pattern NSS = Pattern.compile("^\\d{11}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern CODE = Pattern.compile("^[A-Za-z]{2,5}-\\d{2,4}$");
    private static final Pattern GENERIC_NAME = Pattern.compile("^[\\p{L}0-9][\\p{L}0-9 .,'/-]{2,99}$");
    private static final Pattern DECIMAL = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern POSITIVE_INTEGER = Pattern.compile("^[1-9]\\d*$");

    private InputValidator() {
    }

    public static String validarEmpleado(String id, String nombre, String rfc, String curp, String nss,
                                         String correo, String fechaAlta, String depto, String sueldo) {
        String error = validarIdEmpleado(id);
        if (error != null) return error;

        error = validarNombrePersona(nombre);
        if (error != null) return error;

        error = validarRfc(rfc);
        if (error != null) return error;

        error = validarCurp(curp);
        if (error != null) return error;

        error = validarNss(nss);
        if (error != null) return error;

        error = validarCorreo(correo);
        if (error != null) return error;

        error = validarFecha(fechaAlta);
        if (error != null) return error;

        error = validarDepartamento(depto);
        if (error != null) return error;

        return validarSueldo(sueldo);
    }

    public static String validarIdEmpleado(String id) {
        return validarPatron(id, EMPLOYEE_ID, "El ID debe tener formato E-001.");
    }

    public static String validarNombrePersona(String nombre) {
        return validarPatron(nombre, PERSON_NAME, "El nombre solo debe contener letras y espacios.");
    }

    public static String validarRfc(String rfc) {
        return validarPatron(rfc, RFC, "El RFC debe tener 12 o 13 caracteres con formato valido.");
    }

    public static String validarCurp(String curp) {
        return validarPatron(curp, CURP, "La CURP debe tener 18 caracteres con formato valido.");
    }

    public static String validarNss(String nss) {
        return validarPatron(nss, NSS, "El NSS debe contener exactamente 11 digitos.");
    }

    public static String validarCorreo(String correo) {
        return validarPatron(correo, EMAIL, "El correo electronico no tiene un formato valido.");
    }

    public static String validarFecha(String fecha) {
        return validarPatron(fecha, DATE, "La fecha alta debe tener formato yyyy-MM-dd.");
    }

    public static String validarDepartamento(String depto) {
        return validarPatron(depto, GENERIC_NAME, "El departamento contiene caracteres no permitidos.");
    }

    public static String validarSueldo(String sueldo) {
        return validarDecimalPositivo(sueldo, "El sueldo debe ser un numero positivo con maximo 2 decimales.");
    }

    public static String validarPuesto(String clave, String nombre, String sueldoBase) {
        String error = validarPatron(clave, CODE, "La clave del puesto debe tener formato como PU-01.");
        if (error != null) return error;

        error = validarPatron(nombre, GENERIC_NAME, "El nombre del puesto contiene caracteres no permitidos.");
        if (error != null) return error;

        return validarDecimalPositivo(sueldoBase, "El sueldo base debe ser un numero positivo con maximo 2 decimales.");
    }

    public static String validarPlaza(String clave, String nombre, String depto, String concepto, String monto) {
        String error = validarPatron(clave, CODE, "La clave de la plaza debe tener formato como QA-01.");
        if (error != null) return error;

        error = validarPatron(nombre, GENERIC_NAME, "El nombre de la plaza contiene caracteres no permitidos.");
        if (error != null) return error;

        error = validarPatron(depto, GENERIC_NAME, "El departamento contiene caracteres no permitidos.");
        if (error != null) return error;

        error = validarPatron(concepto, GENERIC_NAME, "El concepto de percepcion contiene caracteres no permitidos.");
        if (error != null) return error;

        return validarDecimalPositivo(monto, "El monto debe ser un numero positivo con maximo 2 decimales.");
    }

    public static String validarPercepcion(String clave, String concepto, String monto) {
        String error = validarPatron(clave, POSITIVE_INTEGER, "La clave de la percepcion debe ser numerica positiva.");
        if (error != null) return error;

        error = validarPatron(concepto, GENERIC_NAME, "El concepto contiene caracteres no permitidos.");
        if (error != null) return error;

        return validarDecimalPositivo(monto, "El monto debe ser un numero positivo con maximo 2 decimales.");
    }

    public static String validarDeduccion(String clave, String concepto, String valor, String tope) {
        String error = validarPatron(clave, POSITIVE_INTEGER, "La clave de la deduccion debe ser numerica positiva.");
        if (error != null) return error;

        error = validarPatron(concepto, GENERIC_NAME, "El concepto contiene caracteres no permitidos.");
        if (error != null) return error;

        error = validarDecimalPositivo(valor, "El valor debe ser un numero positivo con maximo 2 decimales.");
        if (error != null) return error;

        error = validarDecimalNoNegativo(tope, "El tope debe ser un numero mayor o igual a cero con maximo 2 decimales.");
        if (error != null) return error;

        return null;
    }

    private static String validarPatron(String value, Pattern pattern, String message) {
        if (value == null || !pattern.matcher(value.trim()).matches()) {
            return message;
        }
        return null;
    }

    private static String validarDecimalPositivo(String value, String message) {
        if (value == null || !DECIMAL.matcher(value.trim()).matches()) {
            return message;
        }

        if (Double.parseDouble(value.trim()) <= 0) {
            return message;
        }

        return null;
    }

    private static String validarDecimalNoNegativo(String value, String message) {
        if (value == null || !DECIMAL.matcher(value.trim()).matches()) {
            return message;
        }

        if (Double.parseDouble(value.trim()) < 0) {
            return message;
        }

        return null;
    }
}
