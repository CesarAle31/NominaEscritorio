package com.nomina.ui;

import com.nomina.dao.PercepcionDAO;
import com.nomina.model.Percepcion;
import com.nomina.service.TableExportService;
import com.nomina.util.InputValidator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PercepcionesPanel extends JPanel {

    private final PercepcionDAO percepcionDAO;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableExportService tableExportService;
    private final FechaFiltroPanel filtroFechaPanel;

    public PercepcionesPanel() {
        percepcionDAO = new PercepcionDAO();
        tableExportService = new TableExportService();
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Modulo de Percepciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        filtroFechaPanel = new FechaFiltroPanel("Fecha alta", this::aplicarFiltroFecha, this::limpiarFiltroFecha);

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(titulo, BorderLayout.NORTH);
        topPanel.add(filtroFechaPanel, BorderLayout.CENTER);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{
                "Clave", "Concepto", "Tipo", "Base", "Monto", "Fecha alta", "Estatus"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAgregar = new JButton("Agregar percepcion");
        btnAgregar.addActionListener(this::mostrarFormularioAlta);

        JButton btnEliminar = new JButton("Eliminar percepcion");
        btnEliminar.addActionListener(e -> eliminarPercepcionSeleccionada());

        JButton btnCargar = new JButton("Cargar percepciones");
        btnCargar.addActionListener(e -> cargarPercepciones());

        JButton btnExportarPdf = new JButton("Exportar PDF");
        btnExportarPdf.addActionListener(e -> exportarTabla("percepciones", true));

        JButton btnExportarXml = new JButton("Exportar XML");
        btnExportarXml.addActionListener(e -> exportarTabla("percepciones", false));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnAgregar);
        bottomPanel.add(btnEliminar);
        bottomPanel.add(btnCargar);
        bottomPanel.add(btnExportarPdf);
        bottomPanel.add(btnExportarXml);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        cargarPercepciones();
    }

    private void cargarPercepciones() {
        cargarPercepciones(percepcionDAO.listar());
    }

    private void cargarPercepciones(List<Percepcion> percepciones) {
        model.setRowCount(0);

        for (Percepcion percepcion : percepciones) {
            model.addRow(new Object[]{
                    percepcion.getClave(),
                    percepcion.getConcepto(),
                    percepcion.getTipo(),
                    percepcion.getBase(),
                    percepcion.getValor(),
                    percepcion.getFechaAlta(),
                    percepcion.getEstatus()
            });
        }
    }

    private void aplicarFiltroFecha() {
        if (filtroFechaPanel.esTodos()) {
            cargarPercepciones();
            return;
        }

        LocalDate fechaDesde;
        LocalDate fechaHasta = null;

        try {
            fechaDesde = filtroFechaPanel.getFechaDesde();
            if ("Entre fechas".equals(filtroFechaPanel.getFiltroSeleccionado())) {
                fechaHasta = filtroFechaPanel.getFechaHasta();
                if (fechaDesde.isAfter(fechaHasta)) {
                    JOptionPane.showMessageDialog(this, "La fecha desde no puede ser mayor que la fecha hasta.");
                    return;
                }
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }

        List<Percepcion> percepcionesFiltradas = new ArrayList<>();
        for (Percepcion percepcion : percepcionDAO.listar()) {
            try {
                LocalDate fechaAlta = LocalDate.parse(percepcion.getFechaAlta().trim());
                if (filtroFechaPanel.cumpleFiltro(fechaAlta, fechaDesde, fechaHasta)) {
                    percepcionesFiltradas.add(percepcion);
                }
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        cargarPercepciones(percepcionesFiltradas);
    }

    private void limpiarFiltroFecha() {
        filtroFechaPanel.limpiar();
        cargarPercepciones();
    }

    private void mostrarFormularioAlta(ActionEvent event) {
        JTextField txtClave = new JTextField();
        JTextField txtConcepto = new JTextField();
        JTextField txtMonto = new JTextField();
        JTextField txtFechaAlta = new JTextField();
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Activo", "Inactivo"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Clave:"));
        panel.add(txtClave);
        panel.add(new JLabel("Concepto:"));
        panel.add(txtConcepto);
        panel.add(new JLabel("Monto:"));
        panel.add(txtMonto);
        panel.add(new JLabel("Fecha alta (yyyy-MM-dd):"));
        panel.add(txtFechaAlta);
        panel.add(new JLabel("Estatus:"));
        panel.add(cbEstatus);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Agregar percepcion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (tieneCamposVacios(txtClave.getText(), txtConcepto.getText(), txtMonto.getText(), txtFechaAlta.getText())) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        String errorValidacion = InputValidator.validarPercepcion(
                txtClave.getText().trim(),
                txtConcepto.getText().trim(),
                txtMonto.getText().trim()
        );
        if (errorValidacion != null) {
            JOptionPane.showMessageDialog(this, errorValidacion);
            return;
        }

        String errorFecha = InputValidator.validarFecha(txtFechaAlta.getText().trim());
        if (errorFecha != null) {
            JOptionPane.showMessageDialog(this, errorFecha);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(txtMonto.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un numero valido.");
            return;
        }

        Percepcion percepcion = new Percepcion(
                txtClave.getText().trim(),
                txtConcepto.getText().trim(),
                "Bono",
                "Monto Fijo",
                monto,
                txtFechaAlta.getText().trim(),
                String.valueOf(cbEstatus.getSelectedItem())
        );

        boolean agregada = percepcionDAO.agregar(percepcion);
        if (!agregada) {
            JOptionPane.showMessageDialog(this, "Ya existe una percepcion con esa clave.");
            return;
        }

        cargarPercepciones();
        JOptionPane.showMessageDialog(this, "Percepcion agregada correctamente.");
    }

    private void eliminarPercepcionSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una percepcion para eliminar.");
            return;
        }

        String clave = String.valueOf(model.getValueAt(row, 0));
        String concepto = String.valueOf(model.getValueAt(row, 1));

        if ("001".equalsIgnoreCase(clave)) {
            JOptionPane.showMessageDialog(this, "La percepcion de sueldo base no se puede eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar la percepcion " + concepto + " (" + clave + ")?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminada = percepcionDAO.eliminar(clave);
        if (!eliminada) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la percepcion.");
            return;
        }

        cargarPercepciones();
        JOptionPane.showMessageDialog(this, "Percepcion eliminada correctamente.");
    }

    private boolean tieneCamposVacios(String... valores) {
        for (String valor : valores) {
            if (valor == null || valor.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void exportarTabla(String moduleName, boolean pdf) {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para exportar.");
            return;
        }

        try {
            String ruta = pdf
                    ? tableExportService.exportTableToPdf(moduleName, table)
                    : tableExportService.exportTableToXml(moduleName, table);
            JOptionPane.showMessageDialog(this, (pdf ? "PDF" : "XML") + " generado en:\n" + ruta);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
        }
    }
}
