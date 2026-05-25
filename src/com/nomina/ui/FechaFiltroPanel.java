package com.nomina.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FechaFiltroPanel extends JPanel {
    private final JComboBox<String> cbFiltroFecha;
    private final JTextField txtFechaDesde;
    private final JTextField txtFechaHasta;

    public FechaFiltroPanel(String etiquetaFecha, Runnable onFiltrar, Runnable onLimpiar) {
        super(new FlowLayout(FlowLayout.LEFT));

        cbFiltroFecha = new JComboBox<>(new String[]{
                "Todos",
                "Fecha exacta",
                "Antes de",
                "Despues de",
                "Entre fechas"
        });
        txtFechaDesde = new JTextField(10);
        txtFechaHasta = new JTextField(10);

        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnLimpiarFiltro = new JButton("Limpiar filtro");

        cbFiltroFecha.addActionListener(e -> actualizarEstadoFiltros());
        btnFiltrar.addActionListener(e -> onFiltrar.run());
        btnLimpiarFiltro.addActionListener(e -> onLimpiar.run());

        add(new JLabel(etiquetaFecha + ":"));
        add(cbFiltroFecha);
        add(new JLabel("Desde:"));
        add(txtFechaDesde);
        add(new JLabel("Hasta:"));
        add(txtFechaHasta);
        add(new JLabel("Formato yyyy-MM-dd"));
        add(btnFiltrar);
        add(btnLimpiarFiltro);

        actualizarEstadoFiltros();
    }

    public String getFiltroSeleccionado() {
        return String.valueOf(cbFiltroFecha.getSelectedItem());
    }

    public boolean esTodos() {
        return "Todos".equals(getFiltroSeleccionado());
    }

    public LocalDate getFechaDesde() {
        return leerFecha(txtFechaDesde.getText(), "desde");
    }

    public LocalDate getFechaHasta() {
        return leerFecha(txtFechaHasta.getText(), "hasta");
    }

    public void limpiar() {
        cbFiltroFecha.setSelectedItem("Todos");
    }

    public boolean cumpleFiltro(LocalDate fecha, LocalDate fechaDesde, LocalDate fechaHasta) {
        switch (getFiltroSeleccionado()) {
            case "Fecha exacta":
                return fecha.isEqual(fechaDesde);
            case "Antes de":
                return !fecha.isAfter(fechaDesde);
            case "Despues de":
                return !fecha.isBefore(fechaDesde);
            case "Entre fechas":
                return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
            default:
                return true;
        }
    }

    private void actualizarEstadoFiltros() {
        String filtroSeleccionado = getFiltroSeleccionado();
        boolean usaDesde = !"Todos".equals(filtroSeleccionado);
        boolean usaHasta = "Entre fechas".equals(filtroSeleccionado);

        txtFechaDesde.setEnabled(usaDesde);
        txtFechaHasta.setEnabled(usaHasta);

        if (!usaDesde) {
            txtFechaDesde.setText("");
        }
        if (!usaHasta) {
            txtFechaHasta.setText("");
        }
    }

    private LocalDate leerFecha(String valor, String etiqueta) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Captura la fecha " + etiqueta + " con formato yyyy-MM-dd.");
        }

        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha " + etiqueta + " no tiene un formato valido. Usa yyyy-MM-dd.");
        }
    }
}
