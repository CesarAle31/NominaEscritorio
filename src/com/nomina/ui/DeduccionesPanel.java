package com.nomina.ui;

import com.nomina.dao.DeduccionDAO;
import com.nomina.model.Deduccion;
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

public class DeduccionesPanel extends JPanel {

    private final DeduccionDAO deduccionDAO;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableExportService tableExportService;
    private final FechaFiltroPanel filtroFechaPanel;

    public DeduccionesPanel() {
        deduccionDAO = new DeduccionDAO();
        tableExportService = new TableExportService();
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Modulo de Deducciones");
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
                "Clave", "Concepto", "Tipo", "Valor", "Tope", "Obligatoria", "Fecha alta", "Estatus"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAgregar = new JButton("Agregar deduccion");
        btnAgregar.addActionListener(e -> mostrarFormulario(null));

        JButton btnEditar = new JButton("Editar deduccion");
        btnEditar.addActionListener(this::editarSeleccionada);

        JButton btnEliminar = new JButton("Eliminar deduccion");
        btnEliminar.addActionListener(e -> eliminarSeleccionada());

        JButton btnCargar = new JButton("Cargar deducciones");
        btnCargar.addActionListener(e -> cargarDeducciones());

        JButton btnExportarPdf = new JButton("Exportar PDF");
        btnExportarPdf.addActionListener(e -> exportarTabla("deducciones", true));

        JButton btnExportarXml = new JButton("Exportar XML");
        btnExportarXml.addActionListener(e -> exportarTabla("deducciones", false));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnAgregar);
        bottomPanel.add(btnEditar);
        bottomPanel.add(btnEliminar);
        bottomPanel.add(btnCargar);
        bottomPanel.add(btnExportarPdf);
        bottomPanel.add(btnExportarXml);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        cargarDeducciones();
    }

    private void cargarDeducciones() {
        cargarDeducciones(deduccionDAO.listar());
    }

    private void cargarDeducciones(List<Deduccion> deducciones) {
        model.setRowCount(0);

        for (Deduccion deduccion : deducciones) {
            model.addRow(new Object[]{
                    deduccion.getClave(),
                    deduccion.getConcepto(),
                    deduccion.getTipo(),
                    deduccion.getValor(),
                    deduccion.getTope(),
                    deduccion.getObligatoria(),
                    deduccion.getFechaAlta(),
                    deduccion.getEstatus()
            });
        }
    }

    private void aplicarFiltroFecha() {
        if (filtroFechaPanel.esTodos()) {
            cargarDeducciones();
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

        List<Deduccion> deduccionesFiltradas = new ArrayList<>();
        for (Deduccion deduccion : deduccionDAO.listar()) {
            try {
                LocalDate fechaAlta = LocalDate.parse(deduccion.getFechaAlta().trim());
                if (filtroFechaPanel.cumpleFiltro(fechaAlta, fechaDesde, fechaHasta)) {
                    deduccionesFiltradas.add(deduccion);
                }
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        cargarDeducciones(deduccionesFiltradas);
    }

    private void limpiarFiltroFecha() {
        filtroFechaPanel.limpiar();
        cargarDeducciones();
    }

    private void editarSeleccionada(ActionEvent event) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una deduccion para editar.");
            return;
        }

        Deduccion deduccion = new Deduccion(
                String.valueOf(model.getValueAt(row, 0)),
                String.valueOf(model.getValueAt(row, 1)),
                String.valueOf(model.getValueAt(row, 2)),
                Double.parseDouble(String.valueOf(model.getValueAt(row, 3))),
                Double.parseDouble(String.valueOf(model.getValueAt(row, 4))),
                String.valueOf(model.getValueAt(row, 5)),
                String.valueOf(model.getValueAt(row, 6)),
                String.valueOf(model.getValueAt(row, 7))
        );

        mostrarFormulario(deduccion);
    }

    private void mostrarFormulario(Deduccion deduccionActual) {
        boolean edicion = deduccionActual != null;

        JTextField txtClave = new JTextField(edicion ? deduccionActual.getClave() : "");
        JTextField txtConcepto = new JTextField(edicion ? deduccionActual.getConcepto() : "");
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Porcentaje", "Monto Fijo"});
        JTextField txtValor = new JTextField(edicion ? String.valueOf(deduccionActual.getValor()) : "");
        JTextField txtTope = new JTextField(edicion ? String.valueOf(deduccionActual.getTope()) : "0");
        JComboBox<String> cbObligatoria = new JComboBox<>(new String[]{"Si", "No"});
        JTextField txtFechaAlta = new JTextField(edicion ? deduccionActual.getFechaAlta() : "");
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Activo", "Inactivo"});

        if (edicion) {
            txtClave.setEditable(false);
            cbTipo.setSelectedItem(deduccionActual.getTipo());
            cbObligatoria.setSelectedItem(deduccionActual.getObligatoria());
            cbEstatus.setSelectedItem(deduccionActual.getEstatus());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Clave:"));
        panel.add(txtClave);
        panel.add(new JLabel("Concepto:"));
        panel.add(txtConcepto);
        panel.add(new JLabel("Tipo:"));
        panel.add(cbTipo);
        panel.add(new JLabel("Valor:"));
        panel.add(txtValor);
        panel.add(new JLabel("Tope:"));
        panel.add(txtTope);
        panel.add(new JLabel("Obligatoria:"));
        panel.add(cbObligatoria);
        panel.add(new JLabel("Fecha alta (yyyy-MM-dd):"));
        panel.add(txtFechaAlta);
        panel.add(new JLabel("Estatus:"));
        panel.add(cbEstatus);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                edicion ? "Editar deduccion" : "Agregar deduccion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (tieneCamposVacios(txtClave.getText(), txtConcepto.getText(), txtValor.getText(), txtTope.getText(), txtFechaAlta.getText())) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        String errorValidacion = InputValidator.validarDeduccion(
                txtClave.getText().trim(),
                txtConcepto.getText().trim(),
                txtValor.getText().trim(),
                txtTope.getText().trim()
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

        double valor;
        double tope;
        try {
            valor = Double.parseDouble(txtValor.getText().trim());
            tope = Double.parseDouble(txtTope.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor y tope deben ser numeros validos.");
            return;
        }

        Deduccion deduccion = new Deduccion(
                txtClave.getText().trim(),
                txtConcepto.getText().trim(),
                String.valueOf(cbTipo.getSelectedItem()),
                valor,
                tope,
                String.valueOf(cbObligatoria.getSelectedItem()),
                txtFechaAlta.getText().trim(),
                String.valueOf(cbEstatus.getSelectedItem())
        );

        boolean ok = edicion ? deduccionDAO.actualizar(deduccion) : deduccionDAO.agregar(deduccion);
        if (!ok) {
            JOptionPane.showMessageDialog(this, edicion
                    ? "No se pudo actualizar la deduccion."
                    : "Ya existe una deduccion con esa clave.");
            return;
        }

        cargarDeducciones();
        JOptionPane.showMessageDialog(this, edicion
                ? "Deduccion actualizada correctamente."
                : "Deduccion agregada correctamente.");
    }

    private void eliminarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una deduccion para eliminar.");
            return;
        }

        String clave = String.valueOf(model.getValueAt(row, 0));
        String concepto = String.valueOf(model.getValueAt(row, 1));

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar la deduccion " + concepto + " (" + clave + ")?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminada = deduccionDAO.eliminar(clave);
        if (!eliminada) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar la deduccion.");
            return;
        }

        cargarDeducciones();
        JOptionPane.showMessageDialog(this, "Deduccion eliminada correctamente.");
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
