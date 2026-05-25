package com.nomina.controller;

import com.nomina.dao.EmpleadoDAO;
import com.nomina.model.Empleado;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EmpleadoJerarquiaController {
    private final EmpleadoDAO empleadoDAO;

    public EmpleadoJerarquiaController() {
        this.empleadoDAO = new EmpleadoDAO();
    }

    public DefaultMutableTreeNode construirArbol(String departamento) {
        List<Empleado> empleados = filtrarPorDepartamento(empleadoDAO.listar(), departamento);
        Map<String, List<Empleado>> subordinadosPorJefe = agruparPorJefe(empleados);
        Set<String> idsVisibles = new HashSet<>();

        for (Empleado empleado : empleados) {
            idsVisibles.add(empleado.getId());
        }

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Empresa");
        for (Empleado empleado : empleados) {
            String jefeId = normalizar(empleado.getJefeId());
            if (jefeId.isEmpty() || !idsVisibles.contains(jefeId)) {
                raiz.add(construirNodoRecursivo(empleado, subordinadosPorJefe, new HashSet<>()));
            }
        }

        return raiz;
    }

    public List<Empleado> obtenerSubordinadosDirectos(String empleadoId, String departamento) {
        List<Empleado> resultado = new ArrayList<>();
        if (empleadoId == null || empleadoId.trim().isEmpty()) {
            return resultado;
        }

        for (Empleado empleado : filtrarPorDepartamento(empleadoDAO.listar(), departamento)) {
            if (empleadoId.equalsIgnoreCase(normalizar(empleado.getJefeId()))) {
                resultado.add(empleado);
            }
        }

        resultado.sort(Comparator.comparing(Empleado::getNombre, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    public List<String> listarDepartamentos() {
        Set<String> departamentos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Empleado empleado : empleadoDAO.listar()) {
            if (empleado.getDepto() != null && !empleado.getDepto().trim().isEmpty()) {
                departamentos.add(empleado.getDepto().trim());
            }
        }
        return new ArrayList<>(departamentos);
    }

    private DefaultMutableTreeNode construirNodoRecursivo(Empleado empleado,
                                                          Map<String, List<Empleado>> subordinadosPorJefe,
                                                          Set<String> rutaActual) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(empleado);
        String empleadoId = normalizar(empleado.getId());

        if (rutaActual.contains(empleadoId)) {
            return nodo;
        }

        rutaActual.add(empleadoId);
        List<Empleado> subordinados = subordinadosPorJefe.getOrDefault(empleadoId, new ArrayList<>());
        for (Empleado subordinado : subordinados) {
            nodo.add(construirNodoRecursivo(subordinado, subordinadosPorJefe, new HashSet<>(rutaActual)));
        }
        return nodo;
    }

    private Map<String, List<Empleado>> agruparPorJefe(List<Empleado> empleados) {
        Map<String, List<Empleado>> subordinadosPorJefe = new HashMap<>();
        for (Empleado empleado : empleados) {
            String jefeId = normalizar(empleado.getJefeId());
            if (jefeId.isEmpty()) {
                continue;
            }
            subordinadosPorJefe.computeIfAbsent(jefeId, key -> new ArrayList<>()).add(empleado);
        }

        for (List<Empleado> subordinados : subordinadosPorJefe.values()) {
            subordinados.sort(Comparator.comparing(Empleado::getNombre, String.CASE_INSENSITIVE_ORDER));
        }
        return subordinadosPorJefe;
    }

    private List<Empleado> filtrarPorDepartamento(List<Empleado> empleados, String departamento) {
        if (departamento == null || departamento.trim().isEmpty() || "Todos".equalsIgnoreCase(departamento)) {
            return empleados;
        }

        List<Empleado> filtrados = new ArrayList<>();
        for (Empleado empleado : empleados) {
            if (departamento.equalsIgnoreCase(normalizar(empleado.getDepto()))) {
                filtrados.add(empleado);
            }
        }
        return filtrados;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
