package com.nomina.ui;

import com.nomina.controller.EmpleadoJerarquiaController;
import com.nomina.model.Empleado;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class OrganigramaPanel extends JPanel {
    private final EmpleadoJerarquiaController controller;
    private final JTree arbol;
    private final JComboBox<String> cbDepartamento;
    private final DefaultTableModel subordinadosModel;
    private final JTextField txtId;
    private final JTextField txtNombre;
    private final JTextField txtJefe;
    private final JTextField txtPuesto;
    private final JTextField txtPlaza;
    private final JTextField txtDepartamento;
    private final JTextField txtSueldo;
    private final JTextField txtFechaAlta;
    private final JTextField txtEstatus;

    public OrganigramaPanel() {
        controller = new EmpleadoJerarquiaController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titulo = new JLabel("Organigrama empresarial");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        cbDepartamento = new JComboBox<>();
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> recargarArbol());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Departamento:"));
        filtros.add(cbDepartamento);
        filtros.add(btnActualizar);

        JPanel top = new JPanel(new BorderLayout());
        top.add(titulo, BorderLayout.NORTH);
        top.add(filtros, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        arbol = new JTree();
        arbol.setRootVisible(true);
        arbol.setShowsRootHandles(true);
        arbol.setCellRenderer(new EmpleadoTreeCellRenderer());
        arbol.addTreeSelectionListener(e -> mostrarEmpleadoSeleccionado());

        subordinadosModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Puesto", "Departamento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaSubordinados = new JTable(subordinadosModel);

        txtId = crearCampoDetalle();
        txtNombre = crearCampoDetalle();
        txtJefe = crearCampoDetalle();
        txtPuesto = crearCampoDetalle();
        txtPlaza = crearCampoDetalle();
        txtDepartamento = crearCampoDetalle();
        txtSueldo = crearCampoDetalle();
        txtFechaAlta = crearCampoDetalle();
        txtEstatus = crearCampoDetalle();

        JPanel panelDetalle = crearPanelDetalle(tablaSubordinados);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(arbol), panelDetalle);
        splitPane.setResizeWeight(0.48);
        splitPane.setDividerLocation(460);
        add(splitPane, BorderLayout.CENTER);

        cargarDepartamentos();
        recargarArbol();
    }

    private void cargarDepartamentos() {
        cbDepartamento.removeAllItems();
        cbDepartamento.addItem("Todos");
        for (String departamento : controller.listarDepartamentos()) {
            cbDepartamento.addItem(departamento);
        }
    }

    private JPanel crearPanelDetalle(JTable tablaSubordinados) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Detalle del empleado"));

        JPanel datos = new JPanel(new GridBagLayout());
        agregarFila(datos, 0, "ID:", txtId);
        agregarFila(datos, 1, "Nombre:", txtNombre);
        agregarFila(datos, 2, "Jefe ID:", txtJefe);
        agregarFila(datos, 3, "Puesto:", txtPuesto);
        agregarFila(datos, 4, "Plaza:", txtPlaza);
        agregarFila(datos, 5, "Departamento:", txtDepartamento);
        agregarFila(datos, 6, "Sueldo:", txtSueldo);
        agregarFila(datos, 7, "Fecha alta:", txtFechaAlta);
        agregarFila(datos, 8, "Estatus:", txtEstatus);

        JLabel subtitulo = new JLabel("Subordinados directos", SwingConstants.LEFT);
        subtitulo.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel subordinadosPanel = new JPanel(new BorderLayout(4, 4));
        subordinadosPanel.add(subtitulo, BorderLayout.NORTH);
        subordinadosPanel.add(new JScrollPane(tablaSubordinados), BorderLayout.CENTER);

        panel.add(datos, BorderLayout.NORTH);
        panel.add(subordinadosPanel, BorderLayout.CENTER);
        return panel;
    }

    private JTextField crearCampoDetalle() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        return campo;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, JTextField campo) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = fila;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(3, 3, 3, 8);
        panel.add(new JLabel(etiqueta), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = fila;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(3, 3, 3, 3);
        panel.add(campo, fieldConstraints);
    }

    private void recargarArbol() {
        String departamento = String.valueOf(cbDepartamento.getSelectedItem());
        DefaultMutableTreeNode raiz = controller.construirArbol(departamento);
        arbol.setModel(new DefaultTreeModel(raiz));
        expandirPrimerNivel();
        limpiarDetalle();
    }

    private void expandirPrimerNivel() {
        arbol.expandRow(0);
        for (int row = arbol.getRowCount() - 1; row >= 0; row--) {
            if (arbol.getPathForRow(row).getPathCount() <= 2) {
                arbol.expandRow(row);
            }
        }
    }

    private void mostrarEmpleadoSeleccionado() {
        TreePath path = arbol.getSelectionPath();
        if (path == null) {
            limpiarDetalle();
            return;
        }

        Object selected = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (!(selected instanceof Empleado)) {
            limpiarDetalle();
            return;
        }

        Empleado empleado = (Empleado) selected;
        txtId.setText(empleado.getId());
        txtNombre.setText(empleado.getNombre());
        txtJefe.setText(empleado.getJefeId() == null || empleado.getJefeId().trim().isEmpty()
                ? "Sin jefe inmediato"
                : empleado.getJefeId());
        txtPuesto.setText(empleado.getPuesto());
        txtPlaza.setText(empleado.getPlaza());
        txtDepartamento.setText(empleado.getDepto());
        txtSueldo.setText(String.valueOf(empleado.getSueldo()));
        txtFechaAlta.setText(empleado.getFechaAlta());
        txtEstatus.setText(empleado.getEstatus());

        cargarSubordinados(empleado.getId());
    }

    private void cargarSubordinados(String empleadoId) {
        subordinadosModel.setRowCount(0);
        String departamento = String.valueOf(cbDepartamento.getSelectedItem());
        for (Empleado subordinado : controller.obtenerSubordinadosDirectos(empleadoId, departamento)) {
            subordinadosModel.addRow(new Object[]{
                    subordinado.getId(),
                    subordinado.getNombre(),
                    subordinado.getPuesto(),
                    subordinado.getDepto()
            });
        }
    }

    private void limpiarDetalle() {
        txtId.setText("");
        txtNombre.setText("");
        txtJefe.setText("");
        txtPuesto.setText("");
        txtPlaza.setText("");
        txtDepartamento.setText("");
        txtSueldo.setText("");
        txtFechaAlta.setText("");
        txtEstatus.setText("");
        subordinadosModel.setRowCount(0);
    }

    private static class EmpleadoTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof Empleado) {
                Empleado empleado = (Empleado) userObject;
                setText(empleado.getNombre() + " - " + empleado.getPuesto());
            }
            return this;
        }
    }
}
