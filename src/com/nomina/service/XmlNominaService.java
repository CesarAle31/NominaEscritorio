package com.nomina.service;

import com.nomina.model.NominaDetalle;
import com.nomina.model.NominaGenerada;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XmlNominaService {

    public void generarXML(NominaGenerada nomina, String rutaSalida) throws IOException {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Comprobante TipoDeComprobante=\"N\" ");
        xml.append("SubTotal=\"").append(String.format("%.2f", nomina.getTotalPercepciones())).append("\" ");
        xml.append("Total=\"").append(String.format("%.2f", nomina.getNeto())).append("\">\n");

        xml.append("  <Receptor ");
        xml.append("NumEmpleado=\"").append(escape(nomina.getEmpleado().getId())).append("\" ");
        xml.append("Nombre=\"").append(escape(nomina.getEmpleado().getNombre())).append("\" ");
        xml.append("Rfc=\"").append(escape(nomina.getEmpleado().getRfc())).append("\" ");
        xml.append("Curp=\"").append(escape(nomina.getEmpleado().getCurp())).append("\" />\n");

        xml.append("  <Nomina ");
        xml.append("FechaInicialPago=\"").append(nomina.getFechaInicio()).append("\" ");
        xml.append("FechaFinalPago=\"").append(nomina.getFechaFin()).append("\">\n");

        xml.append("    <Percepciones TotalGravado=\"")
                .append(String.format("%.2f", nomina.getTotalPercepciones()))
                .append("\">\n");

        for (NominaDetalle p : nomina.getPercepciones()) {
            xml.append("      <Percepcion Clave=\"").append(escape(p.getClave()))
                    .append("\" Concepto=\"").append(escape(p.getConcepto()))
                    .append("\" Importe=\"").append(String.format("%.2f", p.getImporte()))
                    .append("\" />\n");
        }

        xml.append("    </Percepciones>\n");
        xml.append("    <Deducciones TotalOtrasDeducciones=\"")
                .append(String.format("%.2f", nomina.getTotalDeducciones()))
                .append("\">\n");

        for (NominaDetalle d : nomina.getDeducciones()) {
            xml.append("      <Deduccion Clave=\"").append(escape(d.getClave()))
                    .append("\" Concepto=\"").append(escape(d.getConcepto()))
                    .append("\" Importe=\"").append(String.format("%.2f", d.getImporte()))
                    .append("\" />\n");
        }

        xml.append("    </Deducciones>\n");
        xml.append("  </Nomina>\n");
        xml.append("</Comprobante>\n");

        File archivo = new File(rutaSalida);
        archivo.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(xml.toString());
        }
    }

    private String escape(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}