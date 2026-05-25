package com.nomina.ui;

import com.nomina.dao.PlazaDAO;
import com.nomina.model.Plaza;
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

public class PlazasPanel extends JPanel {

    private final PlazaDAO plazaDAO;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableExportService tableExportService;
    private final FechaFiltroPanel filtroFechaPanel;

    public PlazasPanel() {
        plazaDAO = new PlazaDAO();
        tableExportService = new TableExportService();
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Modulo de Plazas");
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
                "Clave", "Nombre", "Departamento", "Percepcion", "Monto", "Fecha alta", "Estatus"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAgregar = new JButton("Agregar plaza");
        btnAgregar.addActionListener(this::mostrarFormularioAlta);

        JButton btnEliminar = new JButton("Eliminar plaza");
        btnEliminar.addActionListener(e -> eliminarPlazaSeleccionada());

        JButton btnCargar = new JButton("Cargar plazas");
        btnCargar.addActionListener(e -> cargarPlazas());

        JButton btnExportarPdf = new JButton("Exportar PDF");
        btnExportarPdf.addActionListener(e -> exportarTabla("plazas", true));

        JButton btnExportarXml = new JButton("Exportar XML");
        btnExportarXml.addActionListener(e -> exportarTabla("plazas", false));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnAgregar);
        bottomPanel.add(btnEliminar);
        bottomPanel.add(btnCargar);
        bottomPanel.add(btnExportarPdf);
        bottomPanel.add(btnExportarXml);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        cargarPlazas();
    }

    private void cargarPlazas() {
        cargarPlazas(plazaDAO.listar());
    }

    private void cargarPlazas(List<Plaza> plazas) {
        model.setRowCount(0);

        for (Plaza plaza : plazas) {
            model.addRow(new Object[]{
                    plaza.getClave(),
                    plaza.getNombre(),
                    plaza.getDepto(),
                    plaza.getConceptoPercepcion(),
                    plaza.getMontoPercepcion(),
                    plaza.getFechaAlta(),
                    plaza.getEstatus()
            });
        }
    }

    private void aplicarFiltroFecha() {
        if (filtroFechaPanel.esTodos()) {
            cargarPlazas();
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

        List<Plaza> plazasFiltradas = new ArrayList<>();
        for (Plaza plaza : plazaDAO.listar()) {
            try {
                LocalDate fechaAlta = LocalDate.parse(plaza.getFechaAlta().trim());
                if (filtroFechaPanel.cumpleFiltro(fechaAlta, fechaDesde, fechaHasta)) {
                    plazasFiltradas.add(plaza);
                }
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        cargarPlazas(plazasFiltradas);
    }

    private void limpiarFiltroFecha() {
        filtroFechaPanel.limpiar();
        cargarPlazas();
    }

    private void mostrarFormularioAlta(ActionEvent event) {
        JTextField txtClave = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtDepto = new JTextField();
        JTextField txtConcepto = new JTextField();
        JTextField txtMonto = new JTextField();
        JTextField txtFechaAlta = new JTextField();
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Activo", "Inactivo"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Clave:"));
        panel.add(txtClave);
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Departamento:"));
        panel.add(txtDepto);
        panel.add(new JLabel("Concepto percepcion:"));
        panel.add(txtConcepto);
        panel.add(new JLabel("Monto percepcion:"));
        panel.add(txtMonto);
        panel.add(new JLabel("Fecha alta (yyyy-MM-dd):"));
        panel.add(txtFechaAlta);
        panel.add(new JLabel("Estatus:"));
        panel.add(cbEstatus);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Agregar plaza",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (tieneCamposVacios(
                txtClave.getText(), txtNombre.getText(), txtDepto.getText(),
                txtConcepto.getText(), txtMonto.getText(), txtFechaAlta.getText()
        )) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        String errorValidacion = InputValidator.validarPlaza(
                txtClave.getText().trim().toUpperCase(),
                txtNombre.getText().trim(),
                txtDepto.getText().trim(),
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
            JOptionPane.showMessageDialog(this, "El monto de percepcion debe ser un numero valido.");
            return;
        }

        Plaza plaza = new Plaza(
                txtClave.getText().trim().toUpperCase(),
                txtNombre.getText().trim(),
                txtDepto.getText().trim(),
                txtConcepto.getText().trim(),
                monto,
                txtFechaAlta.getText().trim(),
                String.valueOf(cbEstatus.getSelectedItem())
        );

        boolean agregado = plazaDAO.agregar(plaza);
        if (!agregado) {
            JOptionPane.showMessageDialog(this, "Ya existe una plaza con esa clave.");
            return;
        }

        cargarPlazas();
        JOptionPane.showMessageDialog(this, "Plaza agregada correctamente.");
    }

    private void eliminarPlazaSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una plaza para eliminar.");
            return;
        }

        String clave = String.valueOf(model.getValueAt(row, 0));
        String nombre = String.valueOf(model.getValueAt(row, 1));

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar la plaza " + nombre + " (" + clave + ")?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = plazaDAO.eliminar(clave);
        if (!eliminado) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la plaza.");
            return;
        }

        cargarPlazas();
        JOptionPane.showMessageDialog(this, "Plaza eliminada correctamente.");
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
