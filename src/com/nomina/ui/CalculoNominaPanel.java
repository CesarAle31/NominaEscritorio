package com.nomina.ui;

import com.nomina.util.CsvUtil;
import javax.swing.event.TableModelListener;
import com.nomina.dao.DeduccionDAO;
import com.nomina.dao.EmpleadoDAO;
import com.nomina.dao.PercepcionDAO;
import com.nomina.dao.PeriodicidadDAO;
import com.nomina.dao.PlazaDAO;
import com.nomina.dao.SemanaNominaDAO;
import com.nomina.model.Deduccion;
import com.nomina.model.Empleado;
import com.nomina.model.NominaDetalle;
import com.nomina.model.NominaGenerada;
import com.nomina.model.Percepcion;
import com.nomina.model.Plaza;
import com.nomina.model.SemanaNomina;
import com.nomina.service.NominaService;
import com.nomina.service.PdfNominaService;
import com.nomina.service.XmlNominaService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CalculoNominaPanel extends JPanel {

    private JComboBox<Empleado> cbEmpleado;
    private JComboBox<String> cbPeriodo;
    private JComboBox<Integer> cbAnio;
    private JComboBox<SemanaNomina> cbSemana;
    private JTextField txtInicio;
    private JTextField txtFin;

    private JTable tablePercepciones;
    private JTable tableDeducciones;
    private DefaultTableModel modelPercepciones;
    private DefaultTableModel modelDeducciones;

    private JLabel lblTotalPercepciones;
    private JLabel lblTotalDeducciones;
    private JLabel lblNeto;

    private NominaGenerada nominaActual;
    private List<NominaGenerada> nominasActuales;
    private boolean actualizandoTabla;
    private boolean actualizandoSemana;

    private EmpleadoDAO empleadoDAO;
    private PeriodicidadDAO periodicidadDAO;
    private PlazaDAO plazaDAO;
    private SemanaNominaDAO semanaNominaDAO;
    private PercepcionDAO percepcionDAO;
    private DeduccionDAO deduccionDAO;
    private NominaService nominaService;
    private XmlNominaService xmlService;
    private PdfNominaService pdfService;

    public CalculoNominaPanel() {
        empleadoDAO = new EmpleadoDAO();
        periodicidadDAO = new PeriodicidadDAO();
        plazaDAO = new PlazaDAO();
        semanaNominaDAO = new SemanaNominaDAO();
        percepcionDAO = new PercepcionDAO();
        deduccionDAO = new DeduccionDAO();
        nominaService = new NominaService();
        xmlService = new XmlNominaService();
        pdfService = new PdfNominaService();

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 6, 10, 10));

        cbEmpleado = new JComboBox<>();
        cargarComboEmpleados();
        cbEmpleado.addActionListener(e -> cargarConceptosDisponibles());

        cbPeriodo = new JComboBox<>();
        cbPeriodo.setEditable(true);
        cargarComboPeriodos();

        cbAnio = new JComboBox<>();
        cargarComboAnios();
        cbAnio.addActionListener(e -> cargarComboSemanas());

        cbSemana = new JComboBox<>();
        cbSemana.setEditable(true);
        cbSemana.addActionListener(e -> actualizarFechasPorSemana());

        txtInicio = new JTextField();
        txtFin = new JTextField();
        txtInicio.setEditable(false);
        txtFin.setEditable(false);

        topPanel.add(new JLabel("Empleado"));
        topPanel.add(new JLabel("Periodo"));
        topPanel.add(new JLabel("Anio"));
        topPanel.add(new JLabel("Semana"));
        topPanel.add(new JLabel("Fecha inicio"));
        topPanel.add(new JLabel("Fecha fin"));

        topPanel.add(cbEmpleado);
        topPanel.add(cbPeriodo);
        topPanel.add(cbAnio);
        topPanel.add(cbSemana);
        topPanel.add(txtInicio);
        topPanel.add(txtFin);

        modelPercepciones = new DefaultTableModel(
                new String[]{"Aplicar", "Clave", "Concepto", "Base", "Valor", "Importe"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {
                    return !esFilaAutomatica(modelPercepciones, row);
                }
                return column == 4 && !esFilaAutomatica(modelPercepciones, row);
            }
        };

        modelDeducciones = new DefaultTableModel(
                new String[]{"Aplicar", "Clave", "Concepto", "Tipo", "Valor", "Tope", "Importe"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 4;
            }
        };

        tablePercepciones = new JTable(modelPercepciones);
        tableDeducciones = new JTable(modelDeducciones);
        configurarEdicionTablas();

        JPanel percepcionesPanel = new JPanel(new BorderLayout());
        percepcionesPanel.add(new JLabel("Percepciones a aplicar"), BorderLayout.NORTH);
        percepcionesPanel.add(new JScrollPane(tablePercepciones), BorderLayout.CENTER);

        JPanel deduccionesPanel = new JPanel(new BorderLayout());
        deduccionesPanel.add(new JLabel("Deducciones a aplicar"), BorderLayout.NORTH);
        deduccionesPanel.add(new JScrollPane(tableDeducciones), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(percepcionesPanel);
        centerPanel.add(deduccionesPanel);

        JPanel resumenPanel = new JPanel(new GridLayout(3, 1));
        lblTotalPercepciones = new JLabel("Total percepciones: 0.00");
        lblTotalDeducciones = new JLabel("Total deducciones: 0.00");
        lblNeto = new JLabel("Neto: 0.00");

        resumenPanel.add(lblTotalPercepciones);
        resumenPanel.add(lblTotalDeducciones);
        resumenPanel.add(lblNeto);

        JButton btnCalcular = new JButton("Calcular nÃ³mina");
        JButton btnXml = new JButton("Generar XML");
        JButton btnPdf = new JButton("Generar PDF");
        JButton btnCalcularTodos = new JButton("Calcular todos");
        btnCalcular.setText("Calcular empleado");

        btnCalcular.addActionListener(e -> calcularNominaEmpleadoSeleccionado());
        btnCalcularTodos.addActionListener(e -> calcularNominaTodos());
        btnXml.addActionListener(e -> generarXML());
        btnPdf.addActionListener(e -> generarPDF());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnCalcular);
        bottomPanel.add(btnCalcularTodos);
        bottomPanel.add(btnXml);
        bottomPanel.add(btnPdf);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(resumenPanel, BorderLayout.CENTER);
        sur.add(bottomPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        cargarComboSemanas();
        cargarConceptosDisponibles();
    }

//    private void configurarEdicionTablas() {
//        modelPercepciones.addTableModelListener(e -> manejarCambioTabla(e, modelPercepciones, 4));
//        modelDeducciones.addTableModelListener(e -> manejarCambioTabla(e, modelDeducciones, 4));
//    }

    private void configurarEdicionTablas() {
        TableModelListener listenerPercepciones = e -> {
            manejarCambioTabla(e, modelPercepciones, 4);

            if (actualizandoTabla || e.getType() != TableModelEvent.UPDATE || e.getFirstRow() < 0) {
                return;
            }

            if (e.getColumn() == 4) {
                guardarCSV("data/percepciones.csv", modelPercepciones, e.getFirstRow(), 1, 4, 4);
            }
        };

        TableModelListener listenerDeducciones = e -> {
            manejarCambioTabla(e, modelDeducciones, 4);

            if (actualizandoTabla || e.getType() != TableModelEvent.UPDATE || e.getFirstRow() < 0) {
                return;
            }

            if (e.getColumn() == 4) {
                guardarCSV("data/deducciones.csv", modelDeducciones, e.getFirstRow(), 1, 4, 3);
            }
        };

        modelPercepciones.addTableModelListener(listenerPercepciones);
        modelDeducciones.addTableModelListener(listenerDeducciones);
    }

    private void manejarCambioTabla(TableModelEvent event, DefaultTableModel model, int columnaValor) {
        if (actualizandoTabla || event.getType() != TableModelEvent.UPDATE || event.getFirstRow() < 0) {
            return;
        }

        int row = event.getFirstRow();
        int column = event.getColumn();

        if (column == columnaValor) {
            validarValorEditable(model, row, columnaValor);
        }

        if (column == 0 || column == columnaValor) {
            limpiarResumen();
        }
    }

    private void guardarCSV(String archivo, DefaultTableModel model, int row, int columnaClaveTabla,
                            int columnaValorTabla, int columnaValorCsv) {
        String clave = String.valueOf(model.getValueAt(row, columnaClaveTabla));
        String valor = String.valueOf(model.getValueAt(row, columnaValorTabla));

        List<String[]> filas = CsvUtil.leer(archivo);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);

            if (fila.length > 0 && fila[0].equalsIgnoreCase(clave)) {
                fila[columnaValorCsv] = valor;
                CsvUtil.escribir(archivo, filas);
                return;
            }
        }
    }

    private void validarValorEditable(DefaultTableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        String texto = value == null ? "" : String.valueOf(value).trim();
        double numero;

        try {
            numero = Double.parseDouble(texto);
            if (numero < 0) {
                throw new NumberFormatException("negativo");
            }
        } catch (NumberFormatException ex) {
            numero = 0.0;
            JOptionPane.showMessageDialog(this, "El valor debe ser un numero valido mayor o igual a 0.");
        }

        if (value instanceof Number && ((Number) value).doubleValue() == numero) {
            return;
        }

        actualizandoTabla = true;
        try {
            model.setValueAt(numero, row, column);
        } finally {
            actualizandoTabla = false;
        }
    }

    private void cargarComboEmpleados() {
        cbEmpleado.removeAllItems();
        List<Empleado> empleados = empleadoDAO.listar();
        for (Empleado e : empleados) {
            if ("Activo".equalsIgnoreCase(e.getEstatus())) {
                cbEmpleado.addItem(e);
            }
        }

        cbEmpleado.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Empleado) {
                    Empleado emp = (Empleado) value;
                    setText(emp.getId() + " - " + emp.getNombre());
                }
                return this;
            }
        });
    }

    private void cargarConceptosDisponibles() {
        modelPercepciones.setRowCount(0);
        modelDeducciones.setRowCount(0);

        Empleado empleado = (Empleado) cbEmpleado.getSelectedItem();
        if (empleado == null) {
            return;
        }

        double sueldoBase = empleado.getSueldo() / 2.0;
        Plaza plaza = plazaDAO.buscarPorClave(empleado.getPlaza());

        modelPercepciones.addRow(new Object[]{true, "001", "Sueldo Base", "AutomÃ¡tica", sueldoBase, ""});

        if (plaza != null && "Activo".equalsIgnoreCase(plaza.getEstatus()) && plaza.getMontoPercepcion() > 0) {
            modelPercepciones.addRow(new Object[]{
                    true,
                    "PLZ-" + plaza.getClave(),
                    plaza.getConceptoPercepcion(),
                    "Plaza",
                    plaza.getMontoPercepcion(),
                    ""
            });
        }

        for (Percepcion percepcion : percepcionDAO.listar()) {
            if (!"Activo".equalsIgnoreCase(percepcion.getEstatus())) {
                continue;
            }
            if ("001".equalsIgnoreCase(percepcion.getClave())) {
                continue;
            }

            modelPercepciones.addRow(new Object[]{
                    false,
                    percepcion.getClave(),
                    percepcion.getConcepto(),
                    percepcion.getBase(),
                    percepcion.getValor(),
                    ""
            });
        }

        for (Deduccion deduccion : deduccionDAO.listar()) {
            if (!"Activo".equalsIgnoreCase(deduccion.getEstatus())) {
                continue;
            }

            modelDeducciones.addRow(new Object[]{
                    "Si".equalsIgnoreCase(deduccion.getObligatoria()),
                    deduccion.getClave(),
                    deduccion.getConcepto(),
                    deduccion.getTipo(),
                    deduccion.getValor(),
                    deduccion.getTope(),
                    ""
            });
        }

        limpiarResumen();
    }

    private void calcularNominaEmpleadoSeleccionado() {
        detenerEdicionActiva();

        Empleado empleado = (Empleado) cbEmpleado.getSelectedItem();
        if (empleado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado");
            return;
        }

        List<Percepcion> percepcionesSeleccionadas = obtenerPercepcionesSeleccionadas();
        List<Deduccion> deduccionesSeleccionadas = obtenerDeduccionesSeleccionadas();
        guardarPeriodicidadSeleccionada();

        nominaActual = calcularNominaEmpleado(empleado, percepcionesSeleccionadas, deduccionesSeleccionadas);
        nominasActuales = new ArrayList<>();
        nominasActuales.add(nominaActual);

        actualizarImportesCalculados(nominaActual);
        actualizarResumen(nominaActual);
    }

    private void calcularNominaTodos() {
        detenerEdicionActiva();

        List<Percepcion> percepcionesSeleccionadas = obtenerPercepcionesSeleccionadas();
        List<Deduccion> deduccionesSeleccionadas = obtenerDeduccionesSeleccionadas();
        guardarPeriodicidadSeleccionada();
        List<NominaGenerada> nominas = new ArrayList<>();

        for (Empleado empleado : empleadoDAO.listar()) {
            if (!"Activo".equalsIgnoreCase(empleado.getEstatus())) {
                continue;
            }
            nominas.add(calcularNominaEmpleado(empleado, percepcionesSeleccionadas, deduccionesSeleccionadas));
        }

        if (nominas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay empleados activos para calcular.");
            return;
        }

        nominasActuales = nominas;
        nominaActual = null;
        limpiarImportes();
        actualizarResumen(nominas);

        JOptionPane.showMessageDialog(this, "Nomina calculada para " + nominas.size() + " empleados activos.");
    }

    private NominaGenerada calcularNominaEmpleado(Empleado empleado,
                                                  List<Percepcion> percepcionesSeleccionadas,
                                                  List<Deduccion> deduccionesSeleccionadas) {
        Plaza plaza = plazaDAO.buscarPorClave(empleado.getPlaza());
        return nominaService.calcularNomina(
                empleado,
                plaza,
                percepcionesSeleccionadas,
                deduccionesSeleccionadas,
                obtenerPeriodoSeleccionado(),
                txtInicio.getText(),
                txtFin.getText()
        );
    }

    private void actualizarResumen(NominaGenerada nomina) {
        lblTotalPercepciones.setText("Total percepciones: " + String.format("%.2f", nomina.getTotalPercepciones()));
        lblTotalDeducciones.setText("Total deducciones: " + String.format("%.2f", nomina.getTotalDeducciones()));
        lblNeto.setText("Neto: " + String.format("%.2f", nomina.getNeto()));
    }

    private void actualizarResumen(List<NominaGenerada> nominas) {
        double totalPercepciones = 0;
        double totalDeducciones = 0;
        double totalNeto = 0;

        for (NominaGenerada nomina : nominas) {
            totalPercepciones += nomina.getTotalPercepciones();
            totalDeducciones += nomina.getTotalDeducciones();
            totalNeto += nomina.getNeto();
        }

        lblTotalPercepciones.setText("Total percepciones (" + nominas.size() + " empleados): " + String.format("%.2f", totalPercepciones));
        lblTotalDeducciones.setText("Total deducciones (" + nominas.size() + " empleados): " + String.format("%.2f", totalDeducciones));
        lblNeto.setText("Neto total (" + nominas.size() + " empleados): " + String.format("%.2f", totalNeto));
    }

    private List<Percepcion> obtenerPercepcionesSeleccionadas() {
        List<Percepcion> percepciones = new ArrayList<>();

        for (int i = 0; i < modelPercepciones.getRowCount(); i++) {
            boolean aplicar = Boolean.TRUE.equals(modelPercepciones.getValueAt(i, 0));
            String clave = String.valueOf(modelPercepciones.getValueAt(i, 1));
            if (!aplicar || clave.startsWith("PLZ-") || "001".equalsIgnoreCase(clave)) {
                continue;
            }

            percepciones.add(new Percepcion(
                    clave,
                    String.valueOf(modelPercepciones.getValueAt(i, 2)),
                    "Bono",
                    String.valueOf(modelPercepciones.getValueAt(i, 3)),
                    Double.parseDouble(String.valueOf(modelPercepciones.getValueAt(i, 4))),
                    "Activo"
            ));
        }

        return percepciones;
    }

    private List<Deduccion> obtenerDeduccionesSeleccionadas() {
        List<Deduccion> deducciones = new ArrayList<>();

        for (int i = 0; i < modelDeducciones.getRowCount(); i++) {
            boolean aplicar = Boolean.TRUE.equals(modelDeducciones.getValueAt(i, 0));
            if (!aplicar) {
                continue;
            }

            deducciones.add(new Deduccion(
                    String.valueOf(modelDeducciones.getValueAt(i, 1)),
                    String.valueOf(modelDeducciones.getValueAt(i, 2)),
                    String.valueOf(modelDeducciones.getValueAt(i, 3)),
                    Double.parseDouble(String.valueOf(modelDeducciones.getValueAt(i, 4))),
                    Double.parseDouble(String.valueOf(modelDeducciones.getValueAt(i, 5))),
                    "Si",
                    "Activo"
            ));
        }

        return deducciones;
    }

    private void actualizarImportesCalculados(NominaGenerada nomina) {
        limpiarImportes();

        for (NominaDetalle detalle : nomina.getPercepciones()) {
            for (int i = 0; i < modelPercepciones.getRowCount(); i++) {
                String clave = String.valueOf(modelPercepciones.getValueAt(i, 1));
                if (clave.equalsIgnoreCase(detalle.getClave())) {
                    modelPercepciones.setValueAt(String.format("%.2f", detalle.getImporte()), i, 5);
                    break;
                }
            }
        }

        for (NominaDetalle detalle : nomina.getDeducciones()) {
            for (int i = 0; i < modelDeducciones.getRowCount(); i++) {
                String clave = String.valueOf(modelDeducciones.getValueAt(i, 1));
                if (clave.equalsIgnoreCase(detalle.getClave())) {
                    modelDeducciones.setValueAt(String.format("%.2f", detalle.getImporte()), i, 6);
                    break;
                }
            }
        }
    }

    private void limpiarImportes() {
        for (int i = 0; i < modelPercepciones.getRowCount(); i++) {
            modelPercepciones.setValueAt("", i, 5);
        }

        for (int i = 0; i < modelDeducciones.getRowCount(); i++) {
            modelDeducciones.setValueAt("", i, 6);
        }
    }

    private void limpiarResumen() {
        nominaActual = null;
        nominasActuales = null;
        limpiarImportes();
        lblTotalPercepciones.setText("Total percepciones: 0.00");
        lblTotalDeducciones.setText("Total deducciones: 0.00");
        lblNeto.setText("Neto: 0.00");
    }

    private void cargarComboPeriodos() {
        cbPeriodo.removeAllItems();
        for (String periodicidad : periodicidadDAO.listar()) {
            cbPeriodo.addItem(periodicidad);
        }
        cbPeriodo.setSelectedItem("Semanal");
    }

    private void cargarComboAnios() {
        cbAnio.removeAllItems();
        for (Integer anio : semanaNominaDAO.listarAnios()) {
            cbAnio.addItem(anio);
        }
        cbAnio.setSelectedItem(java.time.Year.now().getValue());
    }

    private void cargarComboSemanas() {
        if (cbAnio.getSelectedItem() == null) {
            return;
        }

        actualizandoSemana = true;
        try {
            cbSemana.removeAllItems();
            int anio = (Integer) cbAnio.getSelectedItem();
            for (SemanaNomina semana : semanaNominaDAO.listarPorAnio(anio)) {
                cbSemana.addItem(semana);
            }
        } finally {
            actualizandoSemana = false;
        }

        if (cbSemana.getItemCount() > 0) {
            cbSemana.setSelectedIndex(0);
        }
        actualizarFechasPorSemana();
    }

    private void actualizarFechasPorSemana() {
        if (actualizandoSemana) {
            return;
        }

        SemanaNomina semana = obtenerSemanaSeleccionada();
        if (semana == null) {
            return;
        }

        txtInicio.setText(semana.getFechaInicio());
        txtFin.setText(semana.getFechaFin());
        cbPeriodo.setSelectedItem("Semanal " + String.format("%02d", semana.getNumero()));
        limpiarResumen();
    }

    private SemanaNomina obtenerSemanaSeleccionada() {
        Object selected = cbSemana.getSelectedItem();
        if (selected instanceof SemanaNomina) {
            return (SemanaNomina) selected;
        }

        Object editorItem = cbSemana.getEditor().getItem();
        String texto = editorItem == null ? "" : editorItem.toString().trim();
        if (texto.isEmpty() || cbAnio.getSelectedItem() == null) {
            return null;
        }

        try {
            int numeroSemana = Integer.parseInt(texto.replaceAll("[^0-9]", ""));
            int anio = (Integer) cbAnio.getSelectedItem();
            for (SemanaNomina semana : semanaNominaDAO.listarPorAnio(anio)) {
                if (semana.getNumero() == numeroSemana) {
                    cbSemana.setSelectedItem(semana);
                    return semana;
                }
            }
        } catch (NumberFormatException ex) {
            return null;
        }

        return null;
    }

    private String obtenerPeriodoSeleccionado() {
        Object selected = cbPeriodo.getEditor().getItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private void guardarPeriodicidadSeleccionada() {
        String periodo = obtenerPeriodoSeleccionado();
        if (periodicidadDAO.agregar(periodo)) {
            cbPeriodo.addItem(periodo);
        }
        cbPeriodo.setSelectedItem(periodo);
    }

    private boolean esFilaAutomatica(DefaultTableModel model, int row) {
        String clave = String.valueOf(model.getValueAt(row, 1));
        return "001".equalsIgnoreCase(clave) || clave.startsWith("PLZ-");
    }

    private void detenerEdicionActiva() {
        if (tablePercepciones.isEditing()) {
            tablePercepciones.getCellEditor().stopCellEditing();
        }
        if (tableDeducciones.isEditing()) {
            tableDeducciones.getCellEditor().stopCellEditing();
        }
    }

    private void generarXML() {
        detenerEdicionActiva();

        if (nominaActual == null) {
            JOptionPane.showMessageDialog(this, "Primero calcula la nÃ³mina");
            return;
        }

        try {
            String ruta = "output/xml/Nomina_" + nominaActual.getEmpleado().getId() + ".xml";
            xmlService.generarXML(nominaActual, ruta);
            JOptionPane.showMessageDialog(this, "XML generado en:\n" + ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar XML: " + e.getMessage());
        }
    }

    private void generarPDF() {
        detenerEdicionActiva();

        if (nominaActual == null) {
            JOptionPane.showMessageDialog(this, "Primero calcula la nÃ³mina");
            return;
        }

        try {
            String ruta = "output/pdf/Nomina_" + nominaActual.getEmpleado().getId() + ".pdf";
            pdfService.generarPDF(nominaActual, ruta);
            JOptionPane.showMessageDialog(this, "PDF generado en:\n" + ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar PDF: " + e.getMessage());
        }
    }
}
