# Organigrama de empleados

Vista esperada en el modulo `Organigrama`:

```text
Empresa
└─ MORALES CABALLERO ALBERTO ALEJANDRO - Gerente General
   ├─ BAUTISTA PEREZ OSCAR - Administrador
   │  ├─ ENRIQUEZ CARRILLO BEHTZY PAOLA - Auxiliar Administrativo
   │  ├─ MARTINEZ LOPEZ JULIETA - Auxiliar Administrativo
   │  └─ MENDEZ PENA LIZETH - Administrador
   ├─ HERNANDEZ VALENCIA MANUEL ALFONSO - Contador
   │  └─ POLICARPO SANCHEZ CARLOS GABRIEL - Contador
   ├─ JIMENEZ CRUZ BRYAN - Vendedor
   │  ├─ JUAREZ VILLANUEVA SABDIEL - Cajero
   │  └─ RAMIREZ GONZALEZ ALEXIS JAIR - Vendedor
   ├─ LOPEZ GONZALEZ ANA GABRIELA - Recursos Humanos
   │  └─ SANTOS CARMONA JENNIFER YARISBETH - Recursos Humanos
   ├─ MARTINEZ CHAVEZ CESAR ALEJANDRO - Supervisor
   │  ├─ ANTONIO RAMIREZ FELIPE DE JESUS - Supervisor
   │  │  ├─ DIEGO POLICARPO NISBAN ALEXIS - Mostrador
   │  │  ├─ MARTINEZ PEREZ EDER JAZIEL - Mostrador
   │  │  └─ VASQUEZ ZARATE ERICK DE JESUS - Mostrador
   │  └─ GARCIA GARCIA ALEJANDRO ELIAS - Supervisor
   └─ GUZMAN PEDRO KEVIN - Chofer
      └─ MARTINEZ SANCHEZ FABRICIO EMMANUEL - Chofer
```

Algoritmo recursivo usado:

```java
private DefaultMutableTreeNode construirNodoRecursivo(
        Empleado empleado,
        Map<String, List<Empleado>> subordinadosPorJefe,
        Set<String> rutaActual) {

    DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(empleado);
    String empleadoId = empleado.getId().trim();

    if (rutaActual.contains(empleadoId)) {
        return nodo;
    }

    rutaActual.add(empleadoId);
    for (Empleado subordinado : subordinadosPorJefe.getOrDefault(empleadoId, new ArrayList<>())) {
        nodo.add(construirNodoRecursivo(subordinado, subordinadosPorJefe, new HashSet<>(rutaActual)));
    }
    return nodo;
}
```

La vista usa MVC de esta forma:

- Modelo: `Empleado` con `jefeId`.
- DAO: `EmpleadoDAO` carga y guarda `jefe_id` desde CSV.
- Controlador: `EmpleadoJerarquiaController` agrupa empleados por jefe y construye el arbol.
- Vista: `OrganigramaPanel` muestra `JTree`, filtro por departamento, detalle lateral y tabla de subordinados.
