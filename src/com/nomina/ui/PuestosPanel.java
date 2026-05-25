package com.nomina.ui;

import com.nomina.dao.PuestoDAO;
import com.nomina.model.Puesto;
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

public class PuestosPanel extends JPanel {

    private final PuestoDAO puestoDAO;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableExportService tableExportService;
    private final FechaFiltroPanel filtroFechaPanel;

    public PuestosPanel() {
        puestoDAO = new PuestoDAO();
        tableExportService = new TableExportService();
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Modulo de Puestos");
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
                "Clave", "Nombre", "Sueldo base", "Fecha alta", "Estatus"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAgregar = new JButton("Agregar puesto");
        btnAgregar.addActionListener(this::mostrarFormularioAlta);

        JButton btnEliminar = new JButton("Eliminar puesto");
        btnEliminar.addActionListener(e -> eliminarPuestoSeleccionado());

        JButton btnCargar = new JButton("Cargar puestos");
        btnCargar.addActionListener(e -> cargarPuestos());

        JButton btnExportarPdf = new JButton("Exportar PDF");
        btnExportarPdf.addActionListener(e -> exportarTabla("puestos", true));

        JButton btnExportarXml = new JButton("Exportar XML");
        btnExportarXml.addActionListener(e -> exportarTabla("puestos", false));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnAgregar);
        bottomPanel.add(btnEliminar);
        bottomPanel.add(btnCargar);
        bottomPanel.add(btnExportarPdf);
        bottomPanel.add(btnExportarXml);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        cargarPuestos();
    }

    private void cargarPuestos() {
        cargarPuestos(puestoDAO.listar());
    }

    private void cargarPuestos(List<Puesto> puestos) {
        model.setRowCount(0);

        for (Puesto puesto : puestos) {
            model.addRow(new Object[]{
                    puesto.getClave(),
                    puesto.getNombre(),
                    puesto.getSueldoBase(),
                    puesto.getFechaAlta(),
                    puesto.getEstatus()
            });
        }
    }

    private void aplicarFiltroFecha() {
        if (filtroFechaPanel.esTodos()) {
            cargarPuestos();
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

        List<Puesto> puestosFiltrados = new ArrayList<>();
        for (Puesto puesto : puestoDAO.listar()) {
            try {
                LocalDate fechaAlta = LocalDate.parse(puesto.getFechaAlta().trim());
                if (filtroFechaPanel.cumpleFiltro(fechaAlta, fechaDesde, fechaHasta)) {
                    puestosFiltrados.add(puesto);
                }
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        cargarPuestos(puestosFiltrados);
    }

    private void limpiarFiltroFecha() {
        filtroFechaPanel.limpiar();
        cargarPuestos();
    }

    private void mostrarFormularioAlta(ActionEvent event) {
        JTextField txtClave = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtSueldoBase = new JTextField();
        JTextField txtFechaAlta = new JTextField();
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Activo", "Inactivo"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Clave:"));
        panel.add(txtClave);
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Sueldo base:"));
        panel.add(txtSueldoBase);
        panel.add(new JLabel("Fecha alta (yyyy-MM-dd):"));
        panel.add(txtFechaAlta);
        panel.add(new JLabel("Estatus:"));
        panel.add(cbEstatus);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Agregar puesto",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (tieneCamposVacios(txtClave.getText(), txtNombre.getText(), txtSueldoBase.getText(), txtFechaAlta.getText())) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        String errorValidacion = InputValidator.validarPuesto(
                txtClave.getText().trim().toUpperCase(),
                txtNombre.getText().trim(),
                txtSueldoBase.getText().trim()
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

        double sueldoBase;
        try {
            sueldoBase = Double.parseDouble(txtSueldoBase.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El sueldo base debe ser un numero valido.");
            return;
        }

        Puesto puesto = new Puesto(
                txtClave.getText().trim().toUpperCase(),
                txtNombre.getText().trim(),
                sueldoBase,
                txtFechaAlta.getText().trim(),
                String.valueOf(cbEstatus.getSelectedItem())
        );

        boolean agregado = puestoDAO.agregar(puesto);
        if (!agregado) {
            JOptionPane.showMessageDialog(this, "Ya existe un puesto con esa clave.");
            return;
        }

        cargarPuestos();
        JOptionPane.showMessageDialog(this, "Puesto agregado correctamente.");
    }

    private void eliminarPuestoSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un puesto para eliminar.");
            return;
        }

        String clave = String.valueOf(model.getValueAt(row, 0));
        String nombre = String.valueOf(model.getValueAt(row, 1));

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar el puesto " + nombre + " (" + clave + ")?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = puestoDAO.eliminar(clave);
        if (!eliminado) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el puesto.");
            return;
        }

        cargarPuestos();
        JOptionPane.showMessageDialog(this, "Puesto eliminado correctamente.");
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
