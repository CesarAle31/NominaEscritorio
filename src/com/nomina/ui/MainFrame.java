package com.nomina.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainFrame() {
        setTitle("Sistema de Nómina CSV/XML");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(11, 1, 5, 5));
        menuPanel.setPreferredSize(new Dimension(220, 0));

        JButton btnEmpleados = new JButton("Empleados");
        JButton btnOrganigrama = new JButton("Organigrama");
        JButton btnPuestos = new JButton("Puestos");
        JButton btnPlazas = new JButton("Plazas");
        JButton btnPercepciones = new JButton("Percepciones");
        JButton btnDeducciones = new JButton("Deducciones");
        JButton btnCalculo = new JButton("Cálculo Nómina");
        JButton btnHistorial = new JButton("Historial");

        menuPanel.add(new JLabel("  MÓDULOS"));
        menuPanel.add(btnEmpleados);
        menuPanel.add(btnOrganigrama);
        menuPanel.add(btnPuestos);
        menuPanel.add(btnPlazas);
        menuPanel.add(btnPercepciones);
        menuPanel.add(btnDeducciones);
        menuPanel.add(btnCalculo);
        menuPanel.add(btnHistorial);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(new EmpleadosPanel(), "EMPLEADOS");
        contentPanel.add(new OrganigramaPanel(), "ORGANIGRAMA");
        contentPanel.add(new PuestosPanel(), "PUESTOS");
        contentPanel.add(new PlazasPanel(), "PLAZAS");
        contentPanel.add(new PercepcionesPanel(), "PERCEPCIONES");
        contentPanel.add(new DeduccionesPanel(), "DEDUCCIONES");
        contentPanel.add(new CalculoNominaPanel(), "CALCULO");
        contentPanel.add(new HistorialPanel(), "HISTORIAL");

        btnEmpleados.addActionListener(e -> cardLayout.show(contentPanel, "EMPLEADOS"));
        btnOrganigrama.addActionListener(e -> cardLayout.show(contentPanel, "ORGANIGRAMA"));
        btnPuestos.addActionListener(e -> cardLayout.show(contentPanel, "PUESTOS"));
        btnPlazas.addActionListener(e -> cardLayout.show(contentPanel, "PLAZAS"));
        btnPercepciones.addActionListener(e -> cardLayout.show(contentPanel, "PERCEPCIONES"));
        btnDeducciones.addActionListener(e -> cardLayout.show(contentPanel, "DEDUCCIONES"));
        btnCalculo.addActionListener(e -> cardLayout.show(contentPanel, "CALCULO"));
        btnHistorial.addActionListener(e -> cardLayout.show(contentPanel, "HISTORIAL"));

        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }
}
