package com.nomina.ui;

import javax.swing.*;
import java.awt.*;

public class HistorialPanel extends JPanel {

    public HistorialPanel() {
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Historial de Nóminas");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("Aquí después mostraremos las nóminas guardadas desde historial_nominas.csv");

        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}