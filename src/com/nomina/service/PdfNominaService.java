package com.nomina.service;

import com.nomina.model.NominaDetalle;
import com.nomina.model.NominaGenerada;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class PdfNominaService {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    public void generarPDF(NominaGenerada nomina, String rutaSalida) throws IOException {
        File archivo = new File(rutaSalida);
        archivo.getParentFile().mkdirs();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                dibujarRecibo(content, nomina, page.getMediaBox());
            }

            document.save(archivo);
        }
    }

    public void generarPDF(List<NominaGenerada> nominas, String rutaSalida) throws IOException {
        if (nominas == null || nominas.isEmpty()) {
            throw new IOException("No hay nominas para generar el PDF.");
        }

        File archivo = new File(rutaSalida);
        archivo.getParentFile().mkdirs();

        try (PDDocument document = new PDDocument()) {
            for (NominaGenerada nomina : nominas) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    dibujarRecibo(content, nomina, page.getMediaBox());
                }
            }

            document.save(archivo);
        }
    }

    private void dibujarRecibo(PDPageContentStream content, NominaGenerada nomina, PDRectangle page) throws IOException {
        float margin = 24;
        float pageWidth = page.getWidth() - (margin * 2);
        float top = page.getHeight() - margin;
        float leftWidth = pageWidth * 0.64f;
        float rightWidth = pageWidth - leftWidth;
        float leftX = margin;
        float rightX = margin + leftWidth;

        drawText(content, "Recibo de nomina", leftX, top, 14, true, Color.BLACK);
        drawText(content, "Empleado: " + nomina.getEmpleado().getNombre(), leftX, top - 18, 10, false, Color.BLACK);
        drawText(content, "No. empleado: " + nomina.getEmpleado().getId(), leftX, top - 32, 10, false, Color.BLACK);
        drawText(content, "RFC: " + nomina.getEmpleado().getRfc(), leftX + 220, top - 32, 10, false, Color.BLACK);
        drawText(content, "CURP: " + nomina.getEmpleado().getCurp(), leftX, top - 46, 10, false, Color.BLACK);
        drawText(content, "Periodo: " + nomina.getPeriodo() + "  " + nomina.getFechaInicio() + " a " + nomina.getFechaFin(), leftX, top - 60, 10, false, Color.BLACK);

        float headerY = top - 92;
        drawFilledBox(content, leftX, headerY, leftWidth, 22, new Color(210, 210, 210));
        drawFilledBox(content, rightX, headerY, rightWidth, 22, new Color(210, 210, 210));
        drawBorder(content, leftX, headerY, leftWidth, 22);
        drawBorder(content, rightX, headerY, rightWidth, 22);
        drawText(content, "Percepciones", leftX + (leftWidth / 2) - 40, headerY + 6, 12, true, Color.BLACK);
        drawText(content, "Deducciones", rightX + (rightWidth / 2) - 38, headerY + 6, 12, true, Color.BLACK);

        float tableTop = headerY - 1;
        float tableHeight = 308;
        drawBorder(content, leftX, tableTop - tableHeight, leftWidth, tableHeight);
        drawBorder(content, rightX, tableTop - tableHeight, rightWidth, tableHeight);

        float leftAgrupX = leftX + 4;
        float leftNoX = leftX + 48;
        float leftConceptX = leftX + 90;
        float leftGravadoRight = leftX + leftWidth - 116;
        float leftExentoRight = leftX + leftWidth - 58;
        float leftTotalRight = leftX + leftWidth - 4;
        float leftConceptWidth = leftGravadoRight - leftConceptX - 12;

        float rightAgrupX = rightX + 4;
        float rightNoX = rightX + 48;
        float rightConceptX = rightX + 82;
        float rightTotalRight = rightX + rightWidth - 4;
        float rightConceptWidth = rightTotalRight - rightConceptX - 54;

        drawText(content, "Agrup", leftAgrupX, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "No.", leftNoX, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Concepto", leftConceptX, tableTop - 14, 8, true, Color.BLACK);
        drawTextRight(content, "Gravado", leftGravadoRight, tableTop - 14, 8, true, Color.BLACK);
        drawTextRight(content, "Exento", leftExentoRight, tableTop - 14, 8, true, Color.BLACK);
        drawTextRight(content, "Total", leftTotalRight, tableTop - 14, 8, true, Color.BLACK);

        drawText(content, "Agrup", rightAgrupX, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "No.", rightNoX, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Concepto", rightConceptX, tableTop - 14, 8, true, Color.BLACK);
        drawTextRight(content, "Total", rightTotalRight, tableTop - 14, 8, true, Color.BLACK);

        float leftRowY = tableTop - 38;
        for (NominaDetalle detalle : nomina.getPercepciones()) {
            drawText(content, detalle.getClave().startsWith("PLZ-") ? "OP" : "P", leftAgrupX, leftRowY, 8, false, Color.DARK_GRAY);
            drawText(content, sanitizeClave(detalle.getClave()), leftNoX, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextFit(content, detalle.getConcepto(), leftConceptX, leftRowY, 8, false, Color.DARK_GRAY, leftConceptWidth);
            drawTextRight(content, MONEY.format(detalle.getImporte()), leftGravadoRight, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, "0.00", leftExentoRight, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, MONEY.format(detalle.getImporte()), leftTotalRight, leftRowY, 8, false, Color.DARK_GRAY);
            leftRowY -= 20;
            if (leftRowY < tableTop - tableHeight + 18) {
                break;
            }
        }

        float rightRowY = tableTop - 38;
        for (NominaDetalle detalle : nomina.getDeducciones()) {
            drawText(content, "D", rightAgrupX, rightRowY, 8, false, Color.DARK_GRAY);
            drawText(content, sanitizeClave(detalle.getClave()), rightNoX, rightRowY, 8, false, Color.DARK_GRAY);
            drawTextFit(content, detalle.getConcepto(), rightConceptX, rightRowY, 8, false, Color.DARK_GRAY, rightConceptWidth);
            drawTextRight(content, MONEY.format(detalle.getImporte()), rightTotalRight, rightRowY, 8, false, Color.DARK_GRAY);
            rightRowY -= 20;
            if (rightRowY < tableTop - tableHeight + 18) {
                break;
            }
        }

        float totalsY = tableTop - tableHeight - 6;
        drawBorder(content, leftX, totalsY - 42, leftWidth, 42);
        drawText(content, "Total Percepc. mas Otros Pagos  $", leftX + 8, totalsY - 16, 10, true, Color.BLACK);
        drawTextRight(content, MONEY.format(nomina.getTotalPercepciones()), leftX + leftWidth - 8, totalsY - 16, 10, true, Color.BLACK);

        float rightSummaryY = totalsY - 138;
        drawBorder(content, rightX, rightSummaryY, rightWidth, 138);
        drawMoneyLine(content, "Subtotal $", MONEY.format(nomina.getSubtotal()), rightX, rightWidth, rightSummaryY + 108, 10, true, Color.BLACK);
        drawMoneyLine(content, "Descuentos $", MONEY.format(nomina.getTotalDescuentos()), rightX, rightWidth, rightSummaryY + 84, 10, true, Color.BLACK);
        drawMoneyLine(content, "Retenciones $", MONEY.format(nomina.getTotalRetenciones()), rightX, rightWidth, rightSummaryY + 60, 10, true, Color.BLACK);
        drawMoneyLine(content, "Total $", MONEY.format(nomina.getNeto()), rightX, rightWidth, rightSummaryY + 36, 10, true, Color.BLACK);
        drawMoneyLine(content, "Neto del recibo $", MONEY.format(nomina.getNeto()), rightX, rightWidth, rightSummaryY + 10, 11, true, new Color(65, 105, 225));

        float letrasY = rightSummaryY - 38;
        drawFilledBox(content, rightX + 10, letrasY + 16, rightWidth - 20, 16, new Color(255, 224, 192));
        drawText(content, "Importe con letra", rightX + 92, letrasY + 20, 9, false, Color.BLACK);
        drawText(content, NumeroALetrasUtil.convertirPesos(nomina.getNeto()), rightX + 12, letrasY, 9, false, Color.GRAY);
    }

    private String sanitizeClave(String clave) {
        if (clave == null) return "";
        return clave.replace("PLZ-", "");
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, int size, boolean bold, Color color) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA), size);
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }

    private void drawTextRight(PDPageContentStream content, String text, float x, float y, int size, boolean bold, Color color) throws IOException {
        PDType1Font font = new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
        float width = getTextWidth(text, font, size);
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, size);
        content.newLineAtOffset(x - width, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }

    private void drawTextFit(PDPageContentStream content, String text, float x, float y, int size, boolean bold, Color color, float maxWidth) throws IOException {
        PDType1Font font = new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
        drawText(content, fitText(text, font, size, maxWidth), x, y, size, bold, color);
    }

    private void drawMoneyLine(PDPageContentStream content, String label, String value, float boxX, float boxWidth, float y, int size, boolean bold, Color color) throws IOException {
        PDType1Font font = new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
        float valueRight = boxX + boxWidth - 10;
        float valueWidth = getTextWidth(value, font, size);
        float labelRight = valueRight - valueWidth - 12;
        drawTextRight(content, label, labelRight, y, size, bold, color);
        drawTextRight(content, value, valueRight, y, size, bold, color);
    }

    private String fitText(String text, PDType1Font font, int size, float maxWidth) throws IOException {
        String value = text == null ? "" : text;
        if (getTextWidth(value, font, size) <= maxWidth) {
            return value;
        }

        String suffix = "...";
        float suffixWidth = getTextWidth(suffix, font, size);
        if (suffixWidth > maxWidth) {
            return "";
        }

        while (!value.isEmpty() && getTextWidth(value + suffix, font, size) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + suffix;
    }

    private float getTextWidth(String text, PDType1Font font, int size) throws IOException {
        return font.getStringWidth(text == null ? "" : text) / 1000 * size;
    }

    private void drawBorder(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.setStrokingColor(Color.BLACK);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawFilledBox(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, width, height);
        content.fill();
    }
}
