package com.nomina.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableExportService {

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String exportTableToPdf(String moduleName, JTable table) throws Exception {
        Path outputDir = Paths.get("output", "pdf");
        Files.createDirectories(outputDir);

        String baseName = buildBaseFileName(moduleName);
        Path outputFile = outputDir.resolve(baseName + ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;
            float lineHeight = 14;

            PDPageContentStream content = new PDPageContentStream(document, page);
            content.setLeading(lineHeight);
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            content.newLineAtOffset(margin, y);
            content.showText("Reporte de " + moduleName);
            content.newLine();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            content.showText("Generado: " + LocalDateTime.now().format(DATE_FORMAT));
            content.newLine();
            content.newLine();

            String encabezado = buildHeaderLine(table);
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            content.showText(encabezado);
            content.newLine();

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            for (int row = 0; row < table.getRowCount(); row++) {
                if (y < margin + 60) {
                    content.endText();
                    content.close();

                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    y = page.getMediaBox().getHeight() - margin;

                    content = new PDPageContentStream(document, page);
                    content.setLeading(lineHeight);
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                    content.newLineAtOffset(margin, y);
                }

                content.showText(buildRowLine(table, row));
                content.newLine();
                y -= lineHeight;
            }

            content.endText();
            content.close();
            document.save(outputFile.toFile());
        }

        return outputFile.toString();
    }

    public String exportTableToXml(String moduleName, JTable table) throws Exception {
        Path outputDir = Paths.get("output", "xml");
        Files.createDirectories(outputDir);

        String baseName = buildBaseFileName(moduleName);
        Path outputFile = outputDir.resolve(baseName + ".xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("reporte");
        root.setAttribute("modulo", moduleName);
        root.setAttribute("generado", LocalDateTime.now().format(DATE_FORMAT));
        document.appendChild(root);

        Element registros = document.createElement("registros");
        root.appendChild(registros);

        for (int row = 0; row < table.getRowCount(); row++) {
            Element registro = document.createElement("registro");
            registros.appendChild(registro);

            for (int col = 0; col < table.getColumnCount(); col++) {
                Element field = document.createElement(toXmlTag(table.getColumnName(col)));
                Object value = table.getValueAt(row, col);
                field.setTextContent(value == null ? "" : String.valueOf(value));
                registro.appendChild(field);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(outputFile.toFile()));

        return outputFile.toString();
    }

    private String buildBaseFileName(String moduleName) {
        return sanitizeName(moduleName) + "_" + LocalDateTime.now().format(FILE_FORMAT);
    }

    private String buildHeaderLine(JTable table) {
        StringBuilder builder = new StringBuilder();
        for (int col = 0; col < table.getColumnCount(); col++) {
            if (col > 0) {
                builder.append(" | ");
            }
            builder.append(truncate(table.getColumnName(col), 18));
        }
        return builder.toString();
    }

    private String buildRowLine(JTable table, int row) {
        StringBuilder builder = new StringBuilder();
        for (int col = 0; col < table.getColumnCount(); col++) {
            if (col > 0) {
                builder.append(" | ");
            }
            Object value = table.getValueAt(row, col);
            builder.append(truncate(value == null ? "" : String.valueOf(value), 18));
        }
        return builder.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String toXmlTag(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isEmpty()) {
            return "campo";
        }

        if (Character.isDigit(normalized.charAt(0))) {
            return "campo_" + normalized;
        }

        return normalized;
    }

    private String sanitizeName(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase();
    }
}
