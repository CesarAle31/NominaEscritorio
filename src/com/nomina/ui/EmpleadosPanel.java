package com.nomina.ui;

import com.nomina.dao.EmpleadoDAO;
import com.nomina.dao.PlazaDAO;
import com.nomina.dao.PuestoDAO;
import com.nomina.model.Empleado;
import com.nomina.model.Plaza;
import com.nomina.model.Puesto;
import com.nomina.service.TableExportService;
import com.nomina.util.InputValidator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class EmpleadosPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private EmpleadoDAO empleadoDAO;
    private PuestoDAO puestoDAO;
    private PlazaDAO plazaDAO;
    private TableExportService tableExportService;
    private JComboBox<String> cbFiltroFecha;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;

    public EmpleadosPanel() {
        empleadoDAO = new EmpleadoDAO();
        puestoDAO = new PuestoDAO();
        plazaDAO = new PlazaDAO();
        tableExportService = new TableExportService();
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Modulo de Empleados");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(titulo, BorderLayout.NORTH);
        topPanel.add(crearPanelFiltros(), BorderLayout.CENTER);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{
                "ID", "Nombre", "RFC", "CURP", "Correo", "Jefe ID", "Puesto", "Plaza", "Departamento", "Sueldo", "Fecha ingreso", "Estatus"
        });

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAgregar = new JButton("Agregar empleado");
        btnAgregar.addActionListener(this::mostrarFormularioAlta);

        JButton btnEliminar = new JButton("Eliminar empleado");
        btnEliminar.addActionListener(e -> eliminarEmpleadoSeleccionado());

        JButton btnCargar = new JButton("Cargar empleados");
        btnCargar.addActionListener(e -> cargarEmpleados());

        JButton btnExportarPdf = new JButton("Exportar PDF");
        btnExportarPdf.addActionListener(e -> exportarPdf());

        JButton btnExportarXml = new JButton("Exportar XML");
        btnExportarXml.addActionListener(e -> exportarXml());

        JButton btnGraficarFechas = new JButton("Graficar fechas");
        btnGraficarFechas.addActionListener(e -> mostrarGraficaFechas());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnAgregar);
        bottomPanel.add(btnEliminar);
        bottomPanel.add(btnCargar);
        bottomPanel.add(btnExportarPdf);
        bottomPanel.add(btnExportarXml);
        bottomPanel.add(btnGraficarFechas);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        actualizarEstadoFiltros();
        cargarEmpleados();
    }

    private JPanel crearPanelFiltros() {
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

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
        btnFiltrar.addActionListener(e -> aplicarFiltroFecha());
        btnLimpiarFiltro.addActionListener(e -> limpiarFiltroFecha());

        filtrosPanel.add(new JLabel("Fecha ingreso:"));
        filtrosPanel.add(cbFiltroFecha);
        filtrosPanel.add(new JLabel("Desde:"));
        filtrosPanel.add(txtFechaDesde);
        filtrosPanel.add(new JLabel("Hasta:"));
        filtrosPanel.add(txtFechaHasta);
        filtrosPanel.add(new JLabel("Formato yyyy-MM-dd"));
        filtrosPanel.add(btnFiltrar);
        filtrosPanel.add(btnLimpiarFiltro);

        return filtrosPanel;
    }

    private void cargarEmpleados() {
        cargarEmpleados(empleadoDAO.listar());
    }

    private void cargarEmpleados(List<Empleado> empleados) {
        model.setRowCount(0);

        for (Empleado e : empleados) {
            model.addRow(new Object[]{
                    e.getId(),
                    e.getNombre(),
                    e.getRfc(),
                    e.getCurp(),
                    e.getCorreo(),
                    e.getJefeId(),
                    e.getPuesto(),
                    e.getPlaza(),
                    e.getDepto(),
                    e.getSueldo(),
                    e.getFechaAlta(),
                    e.getEstatus()
            });
        }
    }

    private void actualizarEstadoFiltros() {
        String filtroSeleccionado = String.valueOf(cbFiltroFecha.getSelectedItem());
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

    private void aplicarFiltroFecha() {
        String filtroSeleccionado = String.valueOf(cbFiltroFecha.getSelectedItem());
        if ("Todos".equals(filtroSeleccionado)) {
            cargarEmpleados();
            return;
        }

        LocalDate fechaDesde;
        LocalDate fechaHasta = null;

        try {
            fechaDesde = leerFecha(txtFechaDesde.getText(), "desde");
            if ("Entre fechas".equals(filtroSeleccionado)) {
                fechaHasta = leerFecha(txtFechaHasta.getText(), "hasta");
                if (fechaDesde.isAfter(fechaHasta)) {
                    JOptionPane.showMessageDialog(this, "La fecha desde no puede ser mayor que la fecha hasta.");
                    return;
                }
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }

        List<Empleado> empleadosFiltrados = new ArrayList<>();
        for (Empleado empleado : empleadoDAO.listar()) {
            try {
                LocalDate fechaIngreso = LocalDate.parse(empleado.getFechaAlta().trim());
                if (cumpleFiltroFecha(fechaIngreso, filtroSeleccionado, fechaDesde, fechaHasta)) {
                    empleadosFiltrados.add(empleado);
                }
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        cargarEmpleados(empleadosFiltrados);
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

    private boolean cumpleFiltroFecha(LocalDate fechaIngreso, String filtroSeleccionado,
                                      LocalDate fechaDesde, LocalDate fechaHasta) {
        switch (filtroSeleccionado) {
            case "Fecha exacta":
                return fechaIngreso.isEqual(fechaDesde);
            case "Antes de":
                return !fechaIngreso.isAfter(fechaDesde);
            case "Despues de":
                return !fechaIngreso.isBefore(fechaDesde);
            case "Entre fechas":
                return !fechaIngreso.isBefore(fechaDesde) && !fechaIngreso.isAfter(fechaHasta);
            default:
                return true;
        }
    }

    private void limpiarFiltroFecha() {
        cbFiltroFecha.setSelectedItem("Todos");
        cargarEmpleados();
    }

    private void mostrarGraficaFechas() {
        Map<YearMonth, Integer> altasPorMes = obtenerAltasPorMesDesdeTabla();
        if (altasPorMes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay fechas validas para graficar.");
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Grafica de empleados por fecha");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(new JLabel("Altas de empleados por mes (registros visibles)", SwingConstants.CENTER),
                BorderLayout.NORTH);
        dialog.add(new EmpleadosPorFechaChartPanel(altasPorMes), BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.add(btnCerrar);
        dialog.add(acciones, BorderLayout.SOUTH);

        dialog.setSize(760, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Map<YearMonth, Integer> obtenerAltasPorMesDesdeTabla() {
        Map<YearMonth, Integer> altasPorMes = new TreeMap<>();
        int columnaFechaAlta = 10;

        for (int row = 0; row < table.getRowCount(); row++) {
            Object valorFecha = table.getValueAt(row, columnaFechaAlta);
            if (valorFecha == null) {
                continue;
            }

            try {
                LocalDate fechaAlta = LocalDate.parse(String.valueOf(valorFecha).trim());
                YearMonth mes = YearMonth.from(fechaAlta);
                altasPorMes.put(mes, altasPorMes.getOrDefault(mes, 0) + 1);
            } catch (DateTimeParseException ex) {
                // Ignora registros con fecha invalida.
            }
        }

        return altasPorMes;
    }

    private void exportarPdf() {
        exportarTabla("empleados", true);
    }

    private void exportarXml() {
        exportarTabla("empleados", false);
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

    private void mostrarFormularioAlta(ActionEvent event) {
        JTextField txtId = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtRfc = new JTextField();
        JTextField txtCurp = new JTextField();
        JTextField txtNss = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtFechaAlta = new JTextField();
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        JComboBox<Empleado> cbJefe = new JComboBox<>();
        JComboBox<Puesto> cbPuesto = new JComboBox<>();
        JComboBox<Plaza> cbPlaza = new JComboBox<>();
        JTextField txtPlaza = new JTextField();
        JTextField txtDepto = new JTextField();
        JTextField txtSueldo = new JTextField();

        txtPlaza.setEditable(false);
        txtDepto.setEditable(false);
        txtSueldo.setEditable(false);

        cbJefe.addItem(null);
        for (Empleado empleado : empleadoDAO.listar()) {
            if ("Activo".equalsIgnoreCase(empleado.getEstatus())) {
                cbJefe.addItem(empleado);
            }
        }

        for (Puesto puesto : puestoDAO.listar()) {
            if ("Activo".equalsIgnoreCase(puesto.getEstatus())) {
                cbPuesto.addItem(puesto);
            }
        }

        for (Plaza plaza : plazaDAO.listar()) {
            if ("Activo".equalsIgnoreCase(plaza.getEstatus())) {
                cbPlaza.addItem(plaza);
            }
        }

        cbPuesto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Puesto) {
                    Puesto puesto = (Puesto) value;
                    setText(puesto.getClave() + " - " + puesto.getNombre());
                }
                return this;
            }
        });

        cbPlaza.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Plaza) {
                    Plaza plaza = (Plaza) value;
                    setText(plaza.getClave() + " - " + plaza.getNombre());
                }
                return this;
            }
        });

        cbJefe.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Empleado) {
                    Empleado empleado = (Empleado) value;
                    setText(empleado.getId() + " - " + empleado.getNombre());
                } else {
                    setText("Sin jefe inmediato");
                }
                return this;
            }
        });

        cbPuesto.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarSueldoPuesto((Puesto) e.getItem(), txtSueldo);
            }
        });

        cbPlaza.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarDatosPlaza((Plaza) e.getItem(), txtPlaza, txtDepto);
            }
        });

        if (cbPuesto.getItemCount() > 0) {
            actualizarSueldoPuesto((Puesto) cbPuesto.getItemAt(0), txtSueldo);
        }

        if (cbPlaza.getItemCount() > 0) {
            actualizarDatosPlaza((Plaza) cbPlaza.getItemAt(0), txtPlaza, txtDepto);
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("ID:"));
        panel.add(txtId);
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("RFC:"));
        panel.add(txtRfc);
        panel.add(new JLabel("CURP:"));
        panel.add(txtCurp);
        panel.add(new JLabel("NSS:"));
        panel.add(txtNss);
        panel.add(new JLabel("Correo electronico:"));
        panel.add(txtCorreo);
        panel.add(new JLabel("Jefe inmediato:"));
        panel.add(cbJefe);
        panel.add(new JLabel("Puesto:"));
        panel.add(cbPuesto);
        panel.add(new JLabel("Plaza asignada:"));
        panel.add(cbPlaza);
        panel.add(new JLabel("Clave plaza:"));
        panel.add(txtPlaza);
        panel.add(new JLabel("Departamento:"));
        panel.add(txtDepto);
        panel.add(new JLabel("Sueldo base:"));
        panel.add(txtSueldo);
        panel.add(new JLabel("Fecha alta (yyyy-MM-dd):"));
        panel.add(txtFechaAlta);
        panel.add(new JLabel("Estatus:"));
        panel.add(cbEstatus);

        instalarValidacionEnTiempoReal(txtId, true, value -> InputValidator.validarIdEmpleado(value.toUpperCase()));
        instalarValidacionEnTiempoReal(txtNombre, true, InputValidator::validarNombrePersona);
        instalarValidacionEnTiempoReal(txtRfc, true, value -> InputValidator.validarRfc(value.toUpperCase()));
        instalarValidacionEnTiempoReal(txtCurp, true, value -> InputValidator.validarCurp(value.toUpperCase()));
        instalarValidacionEnTiempoReal(txtNss, true, InputValidator::validarNss);
        instalarValidacionEnTiempoReal(txtCorreo, true, InputValidator::validarCorreo);
        instalarValidacionEnTiempoReal(txtFechaAlta, true, InputValidator::validarFecha);

        boolean confirmado = mostrarDialogoAltaEmpleado(
                panel,
                txtId, txtNombre, txtRfc, txtCurp, txtNss, txtCorreo, txtFechaAlta
        );
        if (!confirmado) {
            return;
        }

        Puesto puestoSeleccionado = (Puesto) cbPuesto.getSelectedItem();
        Plaza plazaSeleccionada = (Plaza) cbPlaza.getSelectedItem();
        Empleado jefeSeleccionado = (Empleado) cbJefe.getSelectedItem();
        if (puestoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debes registrar al menos un puesto activo.");
            return;
        }
        if (plazaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Debes registrar al menos una plaza activa.");
            return;
        }

        if (tieneCamposVacios(
                txtId.getText(), txtNombre.getText(), txtRfc.getText(), txtCurp.getText(),
                txtNss.getText(), txtCorreo.getText(), txtPlaza.getText(), txtDepto.getText(),
                txtSueldo.getText(), txtFechaAlta.getText()
        )) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        String errorValidacion = InputValidator.validarEmpleado(
                txtId.getText().trim(),
                txtNombre.getText().trim(),
                txtRfc.getText().trim().toUpperCase(),
                txtCurp.getText().trim().toUpperCase(),
                txtNss.getText().trim(),
                txtCorreo.getText().trim(),
                txtFechaAlta.getText().trim(),
                txtDepto.getText().trim(),
                txtSueldo.getText().trim()
        );
        if (errorValidacion != null) {
            JOptionPane.showMessageDialog(this, errorValidacion);
            return;
        }

        try {
            LocalDate.parse(txtFechaAlta.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha alta no es una fecha valida.");
            return;
        }

        double sueldo;
        try {
            sueldo = Double.parseDouble(txtSueldo.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El sueldo debe ser un numero valido.");
            return;
        }

        Empleado empleado = new Empleado(
                txtId.getText().trim().toUpperCase(),
                txtNombre.getText().trim(),
                txtRfc.getText().trim().toUpperCase(),
                txtCurp.getText().trim().toUpperCase(),
                txtNss.getText().trim(),
                txtCorreo.getText().trim(),
                jefeSeleccionado == null ? "" : jefeSeleccionado.getId(),
                puestoSeleccionado.getNombre(),
                plazaSeleccionada.getClave(),
                txtDepto.getText().trim(),
                sueldo,
                txtFechaAlta.getText().trim(),
                String.valueOf(cbEstatus.getSelectedItem())
        );

        boolean agregado = empleadoDAO.agregar(empleado);
        if (!agregado) {
            JOptionPane.showMessageDialog(this, "Ya existe un empleado con ese ID.");
            return;
        }

        cargarEmpleados();
        JOptionPane.showMessageDialog(this, "Empleado agregado correctamente.");
    }

    private void eliminarEmpleadoSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para eliminar.");
            return;
        }

        String idEmpleado = String.valueOf(model.getValueAt(row, 0));
        String nombreEmpleado = String.valueOf(model.getValueAt(row, 1));

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar al empleado " + nombreEmpleado + " (" + idEmpleado + ")?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = empleadoDAO.eliminar(idEmpleado);
        if (!eliminado) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el empleado.");
            return;
        }

        cargarEmpleados();
        JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.");
    }

    private boolean tieneCamposVacios(String... valores) {
        for (String valor : valores) {
            if (valor == null || valor.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void actualizarSueldoPuesto(Puesto puesto, JTextField txtSueldo) {
        txtSueldo.setText(String.valueOf(puesto.getSueldoBase()));
    }

    private void actualizarDatosPlaza(Plaza plaza, JTextField txtPlaza, JTextField txtDepto) {
        txtPlaza.setText(plaza.getClave());
        txtDepto.setText(plaza.getDepto());
    }

    private boolean mostrarDialogoAltaEmpleado(JPanel panel, JTextField txtId, JTextField txtNombre,
                                               JTextField txtRfc, JTextField txtCurp, JTextField txtNss,
                                               JTextField txtCorreo, JTextField txtFechaAlta) {
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION
        );
        JDialog dialog = optionPane.createDialog(this, "Agregar empleado");
        dialog.setModal(true);

        optionPane.addPropertyChangeListener(evt -> {
            if (!dialog.isVisible() || !JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
                return;
            }

            Object value = optionPane.getValue();
            if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(value)) {
                dialog.dispose();
                return;
            }

            String mensajeError = validarCamposFormularioAlta(
                    txtId, txtNombre, txtRfc, txtCurp, txtNss, txtCorreo, txtFechaAlta
            );
            if (mensajeError == null) {
                dialog.dispose();
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            JOptionPane.showMessageDialog(dialog, mensajeError);
        });

        dialog.pack();
        dialog.setVisible(true);
        return Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue());
    }

    private String validarCamposFormularioAlta(JTextField txtId, JTextField txtNombre, JTextField txtRfc,
                                               JTextField txtCurp, JTextField txtNss, JTextField txtCorreo,
                                               JTextField txtFechaAlta) {
        String error = validarCampoEnPantalla(txtId, true, value -> InputValidator.validarIdEmpleado(value.toUpperCase()));
        if (error != null) return error;

        error = validarCampoEnPantalla(txtNombre, true, InputValidator::validarNombrePersona);
        if (error != null) return error;

        error = validarCampoEnPantalla(txtRfc, true, value -> InputValidator.validarRfc(value.toUpperCase()));
        if (error != null) return error;

        error = validarCampoEnPantalla(txtCurp, true, value -> InputValidator.validarCurp(value.toUpperCase()));
        if (error != null) return error;

        error = validarCampoEnPantalla(txtNss, true, InputValidator::validarNss);
        if (error != null) return error;

        error = validarCampoEnPantalla(txtCorreo, true, InputValidator::validarCorreo);
        if (error != null) return error;

        return validarCampoEnPantalla(txtFechaAlta, true, InputValidator::validarFecha);
    }

    private void instalarValidacionEnTiempoReal(JTextField campo, boolean obligatorio,
                                                Function<String, String> validador) {
        Border bordeNormal = campo.getBorder();
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validar();
            }

            private void validar() {
                actualizarEstadoCampo(campo, bordeNormal, obligatorio, validador);
            }
        });
        actualizarEstadoCampo(campo, bordeNormal, obligatorio, validador);
    }

    private String validarCampoEnPantalla(JTextField campo, boolean obligatorio,
                                          Function<String, String> validador) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        if (valor.isEmpty()) {
            return obligatorio ? "Todos los campos son obligatorios." : null;
        }
        return validador.apply(valor);
    }

    private void actualizarEstadoCampo(JTextField campo, Border bordeNormal, boolean obligatorio,
                                       Function<String, String> validador) {
        String error = validarCampoEnPantalla(campo, obligatorio, validador);
        if (error == null) {
            campo.setBorder(bordeNormal);
            campo.setToolTipText(null);
            return;
        }

        campo.setBorder(BorderFactory.createLineBorder(Color.RED));
        campo.setToolTipText(error);
    }

    private static class EmpleadosPorFechaChartPanel extends JPanel {
        private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");
        private final Map<YearMonth, Integer> altasPorMes;

        EmpleadosPorFechaChartPanel(Map<YearMonth, Integer> altasPorMes) {
            this.altasPorMes = altasPorMes;
            setPreferredSize(new Dimension(720, 340));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 56;
            int right = 24;
            int top = 24;
            int bottom = 72;
            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            if (chartWidth <= 0 || chartHeight <= 0) {
                g.dispose();
                return;
            }

            int max = 1;
            for (Integer total : altasPorMes.values()) {
                max = Math.max(max, total);
            }

            g.setColor(new Color(245, 247, 250));
            g.fillRect(left, top, chartWidth, chartHeight);
            g.setColor(new Color(210, 215, 222));
            g.drawRect(left, top, chartWidth, chartHeight);

            dibujarLineasGuia(g, left, top, chartWidth, chartHeight, max);
            dibujarBarras(g, left, top, chartWidth, chartHeight, max);

            g.dispose();
        }

        private void dibujarLineasGuia(Graphics2D g, int left, int top, int chartWidth, int chartHeight, int max) {
            g.setFont(new Font("Arial", Font.PLAIN, 11));

            for (int i = 0; i <= max; i++) {
                int y = top + chartHeight - (int) Math.round((double) i / max * chartHeight);
                g.setColor(new Color(226, 232, 240));
                g.drawLine(left, y, left + chartWidth, y);
                g.setColor(new Color(80, 80, 80));
                g.drawString(String.valueOf(i), 24, y + 4);
            }
        }

        private void dibujarBarras(Graphics2D g, int left, int top, int chartWidth, int chartHeight, int max) {
            int count = altasPorMes.size();
            int gap = 12;
            int barWidth = Math.max(18, (chartWidth - gap * (count + 1)) / count);
            int x = left + gap;

            g.setFont(new Font("Arial", Font.PLAIN, 11));
            for (Map.Entry<YearMonth, Integer> entry : altasPorMes.entrySet()) {
                int total = entry.getValue();
                int barHeight = (int) Math.round((double) total / max * (chartHeight - 8));
                int y = top + chartHeight - barHeight;

                g.setColor(new Color(43, 117, 197));
                g.fillRect(x, y, barWidth, barHeight);
                g.setColor(new Color(28, 72, 124));
                g.drawRect(x, y, barWidth, barHeight);

                String totalText = String.valueOf(total);
                int totalWidth = g.getFontMetrics().stringWidth(totalText);
                g.setColor(new Color(30, 30, 30));
                g.drawString(totalText, x + (barWidth - totalWidth) / 2, y - 6);

                String label = entry.getKey().format(LABEL_FORMAT);
                dibujarEtiquetaVertical(g, label, x + barWidth / 2, top + chartHeight + 8);

                x += barWidth + gap;
            }
        }

        private void dibujarEtiquetaVertical(Graphics2D g, String label, int x, int y) {
            Graphics2D rotated = (Graphics2D) g.create();
            rotated.rotate(-Math.PI / 4, x, y);
            rotated.drawString(label, x - 18, y + 18);
            rotated.dispose();
        }
    }
}
