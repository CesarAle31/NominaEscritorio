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

        drawText(content, "Agrup", leftX + 4, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "No.", leftX + 48, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Concepto", leftX + 90, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Gravado", leftX + 225, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Exento", leftX + 300, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Total", leftX + 378, tableTop - 14, 8, true, Color.BLACK);

        drawText(content, "Agrup", rightX + 4, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "No.", rightX + 48, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Concepto", rightX + 90, tableTop - 14, 8, true, Color.BLACK);
        drawText(content, "Total", rightX + rightWidth - 52, tableTop - 14, 8, true, Color.BLACK);

        float leftRowY = tableTop - 38;
        for (NominaDetalle detalle : nomina.getPercepciones()) {
            drawText(content, detalle.getClave().startsWith("PLZ-") ? "OP" : "P", leftX + 4, leftRowY, 8, false, Color.DARK_GRAY);
            drawText(content, sanitizeClave(detalle.getClave()), leftX + 48, leftRowY, 8, false, Color.DARK_GRAY);
            drawText(content, detalle.getConcepto(), leftX + 90, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, MONEY.format(detalle.getImporte()), leftX + 300, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, "0.00", leftX + 360, leftRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, MONEY.format(detalle.getImporte()), leftX + leftWidth - 4, leftRowY, 8, false, Color.DARK_GRAY);
            leftRowY -= 20;
            if (leftRowY < tableTop - tableHeight + 18) {
                break;
            }
        }

        float rightRowY = tableTop - 38;
        for (NominaDetalle detalle : nomina.getDeducciones()) {
            drawText(content, "D", rightX + 4, rightRowY, 8, false, Color.DARK_GRAY);
            drawText(content, sanitizeClave(detalle.getClave()), rightX + 48, rightRowY, 8, false, Color.DARK_GRAY);
            drawText(content, detalle.getConcepto(), rightX + 90, rightRowY, 8, false, Color.DARK_GRAY);
            drawTextRight(content, MONEY.format(detalle.getImporte()), rightX + rightWidth - 4, rightRowY, 8, false, Color.DARK_GRAY);
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
        drawText(content, "Subtotal $", rightX + 110, rightSummaryY + 108, 10, true, Color.BLACK);
        drawTextRight(content, MONEY.format(nomina.getSubtotal()), rightX + rightWidth - 10, rightSummaryY + 108, 10, true, Color.BLACK);
        drawText(content, "Descuentos $", rightX + 90, rightSummaryY + 84, 10, true, Color.BLACK);
        drawTextRight(content, MONEY.format(nomina.getTotalDescuentos()), rightX + rightWidth - 10, rightSummaryY + 84, 10, true, Color.BLACK);
        drawText(content, "Retenciones $", rightX + 86, rightSummaryY + 60, 10, true, Color.BLACK);
        drawTextRight(content, MONEY.format(nomina.getTotalRetenciones()), rightX + rightWidth - 10, rightSummaryY + 60, 10, true, Color.BLACK);
        drawText(content, "Total $", rightX + 132, rightSummaryY + 36, 10, true, Color.BLACK);
        drawTextRight(content, MONEY.format(nomina.getNeto()), rightX + rightWidth - 10, rightSummaryY + 36, 10, true, Color.BLACK);
        drawText(content, "Neto del recibo $", rightX + 68, rightSummaryY + 10, 11, true, new Color(65, 105, 225));
        drawTextRight(content, MONEY.format(nomina.getNeto()), rightX + rightWidth - 10, rightSummaryY + 10, 11, true, new Color(65, 105, 225));

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
        float width = font.getStringWidth(text) / 1000 * size;
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, size);
        content.newLineAtOffset(x - width, y);
        content.showText(text == null ? "" : text);
        content.endText();
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
