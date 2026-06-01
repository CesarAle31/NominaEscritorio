package com.nomina.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Grafica de pastel dibujada con Java2D (sin dependencias externas).
 * Cada rebanada representa el valor de una etiqueta; el tamano es
 * proporcional al valor, por lo que la rebanada mas grande corresponde
 * a quien tiene el mayor monto.
 */
public class PieChartPanel extends JPanel {

    public static class Rebanada {
        final String etiqueta;
        final double valor;
        Color color;

        public Rebanada(String etiqueta, double valor) {
            this.etiqueta = etiqueta;
            this.valor = valor;
        }
    }

    private final List<Rebanada> rebanadas = new ArrayList<>();
    private final List<AreaRebanada> areas = new ArrayList<>();
    private double total;
    private int indiceActivo = -1;

    public PieChartPanel() {
        setBackground(Color.WHITE);
        setToolTipText("");

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int nuevoIndice = buscarIndice(e.getPoint());
                if (nuevoIndice != indiceActivo) {
                    indiceActivo = nuevoIndice;
                    setCursor(indiceActivo >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (indiceActivo >= 0) {
                    indiceActivo = -1;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int indice = buscarIndice(e.getPoint());
                if (indice >= 0) {
                    Rebanada rebanada = rebanadas.get(indice);
                    JOptionPane.showMessageDialog(
                            PieChartPanel.this,
                            crearMensajeDetalle(rebanada),
                            "Detalle del empleado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        };
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }

    public void setDatos(List<Rebanada> datos) {
        rebanadas.clear();
        rebanadas.addAll(datos);
        rebanadas.sort(Comparator.comparingDouble((Rebanada r) -> r.valor).reversed());

        total = 0;
        for (Rebanada r : rebanadas) {
            total += r.valor;
        }

        for (int i = 0; i < rebanadas.size(); i++) {
            float hue = (float) i / Math.max(1, rebanadas.size());
            rebanadas.get(i).color = Color.getHSBColor(hue, 0.65f, 0.9f);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (rebanadas.isEmpty() || total <= 0) {
            areas.clear();
            g.setColor(Color.GRAY);
            g.drawString("No hay datos para graficar.", 20, 30);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int leyendaAncho = 320;
        int margen = 20;
        int diametro = Math.min(getWidth() - leyendaAncho - margen * 2, getHeight() - margen * 2);
        diametro = Math.max(diametro, 80);

        int x = margen;
        int y = (getHeight() - diametro) / 2;

        areas.clear();
        double inicioAngulo = 90;
        for (int i = 0; i < rebanadas.size(); i++) {
            Rebanada r = rebanadas.get(i);
            double extension = -(r.valor / total) * 360.0;
            Arc2D.Double arco = new Arc2D.Double(x, y, diametro, diametro, inicioAngulo, extension, Arc2D.PIE);
            areas.add(new AreaRebanada(arco, null));

            g2.setColor(r.color);
            g2.fill(arco);
            g2.setColor(Color.WHITE);
            g2.draw(arco);
            if (i == indiceActivo) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(arco);
                g2.setStroke(new BasicStroke(1f));
            }
            inicioAngulo += extension;
        }

        dibujarLeyenda(g2, x + diametro + margen, y);

        g2.dispose();
    }

    private void dibujarLeyenda(Graphics2D g2, int x, int y) {
        int cuadro = 14;
        int lineaAlto = 22;
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < rebanadas.size(); i++) {
            Rebanada r = rebanadas.get(i);
            int fila = y + i * lineaAlto;

            Rectangle2D.Double areaLeyenda = new Rectangle2D.Double(x - 4, fila - 3, getWidth() - x - 12, lineaAlto);
            if (i < areas.size()) {
                areas.get(i).leyenda = areaLeyenda;
            }
            if (i == indiceActivo) {
                g2.setColor(new Color(0, 0, 0, 28));
                g2.fill(areaLeyenda);
            }

            g2.setColor(r.color);
            g2.fillRect(x, fila, cuadro, cuadro);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, fila, cuadro, cuadro);

            double porcentaje = (r.valor / total) * 100.0;
            String texto = String.format("%s  $%,.2f (%.1f%%)", r.etiqueta, r.valor, porcentaje);
            g2.setColor(Color.BLACK);
            g2.drawString(texto, x + cuadro + 6, fila + cuadro - 2 + (fm.getAscent() - cuadro) / 2);
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int indice = buscarIndice(event.getPoint());
        if (indice < 0) {
            return null;
        }
        Rebanada rebanada = rebanadas.get(indice);
        return "<html>" + crearMensajeDetalle(rebanada).replace("\n", "<br>") + "</html>";
    }

    private int buscarIndice(Point point) {
        for (int i = 0; i < areas.size(); i++) {
            AreaRebanada area = areas.get(i);
            if (area.pastel.contains(point) || (area.leyenda != null && area.leyenda.contains(point))) {
                return i;
            }
        }
        return -1;
    }

    private String crearMensajeDetalle(Rebanada rebanada) {
        double porcentaje = total > 0 ? (rebanada.valor / total) * 100.0 : 0;
        return String.format(
                "Empleado: %s%nNeto: $%,.2f%nParticipacion: %.1f%%",
                rebanada.etiqueta,
                rebanada.valor,
                porcentaje
        );
    }

    private static class AreaRebanada {
        final Arc2D pastel;
        Rectangle2D leyenda;

        AreaRebanada(Arc2D pastel, Rectangle2D leyenda) {
            this.pastel = pastel;
            this.leyenda = leyenda;
        }
    }
}
