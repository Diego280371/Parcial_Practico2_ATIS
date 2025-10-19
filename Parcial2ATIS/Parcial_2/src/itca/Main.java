/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itca;

/**
 *
 * @author Diego Mejia
 */

import itca.Validaciones.ConflictoReservaException;
import itca.Validaciones.RecursoNoEncontradoException;
import itca.Validaciones.ValidacionException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    // ------------------- Utilidades de consola -------------------
    private static void pausa() {
        System.out.print("\nPresiona ENTER para continuar...");
        sc.nextLine();
    }

    private static String txt(String label, boolean requerido) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine();
            if (!requerido) return s == null ? "" : s.trim();
            if (s != null && !s.trim().isEmpty()) return s.trim();
            System.out.println("Campo requerido.");
        }
    }

    private static int num(String label, int min, int max) {
        while (true) {
            try {
                System.out.print(label);
                String s = sc.nextLine();
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (Exception e) {
                System.out.printf("Ingresa un número entre %d y %d.\n", min, max);
            }
        }
    }

    private static LocalDate fecha(String label) {
        while (true) {
            try {
                System.out.print(label + " (yyyy-MM-dd): ");
                return LocalDate.parse(sc.nextLine().trim(), DataStore.F_FECHA);
            } catch (Exception e) {
                System.out.println("Fecha inválida. Ej: 2025-05-21");
            }
        }
    }

    private static LocalTime hora(String label) {
        while (true) {
            try {
                System.out.print(label + " (HH:mm): ");
                return LocalTime.parse(sc.nextLine().trim(), DataStore.F_HORA);
            } catch (Exception e) {
                System.out.println("Hora inválida. Ej: 08:30");
            }
        }
    }

    private static String trunc(String s, int n) {
        if (s == null) return "";
        s = s.trim();
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    // ------------------- Menú Aulas -------------------
    private static void menuAulas() {
        while (true) {
            System.out.println("\n-- Aulas --");
            System.out.println("1) Registrar");
            System.out.println("2) Listar");
            System.out.println("3) Modificar");
            System.out.println("0) Volver");
            int op = num("Opción: ", 0, 3);
            switch (op) {
                case 1:
                    registrarAula();
                    break;
                case 2:
                    listarAulas();
                    break;
                case 3:
                    modificarAula();
                    break;
                default:
                    return;
            }
        }
    }

    private static void registrarAula() {
        try {
            String cod = txt("Código: ", true);
            // verificar duplicado
            boolean existe = DataStore.AULAS.stream()
                    .anyMatch(a -> a.getCodigo().equalsIgnoreCase(cod));
            if (existe) {
                System.out.println("Ya existe un aula con ese código.");
                pausa();
                return;
            }

            String nom = txt("Nombre: ", true);
            System.out.println("Tipo [1=TEORICA, 2=LABORATORIO, 3=AUDITORIO]");
            int t = num("Tipo: ", 1, 3);
            TipoAula tipo = (t == 1) ? TipoAula.TEORICA : (t == 2 ? TipoAula.LABORATORIO : TipoAula.AUDITORIO);
            int cap = num("Capacidad: ", 1, 10000);

            Aula a = new Aula(cod, nom, tipo, cap);
            a.validar();

            DataStore.AULAS.add(a);
            DataStore.saveAulas();
            System.out.println("Aula registrada.");
        } catch (ValidacionException e) {
            System.out.println("Validación: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pausa();
    }

    private static void listarAulas() {
        if (DataStore.AULAS.isEmpty()) {
            System.out.println("No hay aulas registradas.");
        } else {
            System.out.println("\nCODIGO   | NOMBRE               | TIPO        | CAP");
            System.out.println("-----------------------------------------------");
            for (Aula a : DataStore.AULAS) {
                System.out.println(a.toString());
            }
        }
        pausa();
    }

    private static void modificarAula() {
        try {
            String cod = txt("Código del aula a modificar: ", true);
            Aula a = DataStore.buscarAula(cod);

            String nom = txt("Nuevo nombre (enter mantiene): ", false);
            if (nom != null && !nom.trim().isEmpty()) a.setNombre(nom.trim());

            System.out.println("Nuevo tipo [1=TEORICA, 2=LABORATORIO, 3=AUDITORIO, 0=mantener]");
            int t = num("Tipo: ", 0, 3);
            if (t != 0) {
                a.setTipo(t == 1 ? TipoAula.TEORICA : (t == 2 ? TipoAula.LABORATORIO : TipoAula.AUDITORIO));
            }

            int cap = num("Nueva capacidad (0 mantiene): ", 0, 10000);
            if (cap != 0) a.setCapacidad(cap);

            a.validar();
            DataStore.saveAulas();
            System.out.println("Aula modificada.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pausa();
    }

    // ------------------- Menú Reservas -------------------
    private static void menuReservas() {
        while (true) {
            System.out.println("\n-- Reservas --");
            System.out.println("1) Nueva");
            System.out.println("2) Listar / Buscar / Ordenar");
            System.out.println("3) Modificar");
            System.out.println("4) Cancelar");
            System.out.println("0) Volver");
            int op = num("Opción: ", 0, 4);
            switch (op) {
                case 1:
                    nuevaReserva();
                    break;
                case 2:
                    listarBuscarOrdenar();
                    break;
                case 3:
                    modificarReserva();
                    break;
                case 4:
                    cancelarReserva();
                    break;
                default:
                    return;
            }
        }
    }

    private static void nuevaReserva() {
        try {
            if (DataStore.AULAS.isEmpty()) {
                System.out.println("Primero registra aulas.");
                pausa();
                return;
            }

            String aula = txt("Código de aula: ", true);
            DataStore.buscarAula(aula); // valida existencia
            LocalDate f = fecha("Fecha");
            LocalTime hi = hora("Hora inicio");
            LocalTime hf = hora("Hora fin");
            String resp = txt("Responsable: ", true);

            System.out.println("Tipo de reserva [1=CLASE, 2=EVENTO, 3=PRACTICA]");
            int tipo = num("Tipo: ", 1, 3);

            String id = java.util.UUID.randomUUID().toString();
            Reserva r;

            if (tipo == 1) {
                String materia = txt("Materia: ", true);
                String grupo = txt("Grupo: ", true);
                r = new ReservaClase(id, aula, f, hi, hf, resp, EstadoReserva.ACTIVA, materia, grupo);
            } else if (tipo == 2) {
                System.out.println("Tipo evento [1=CONFERENCIA, 2=TALLER, 3=REUNION]");
                int te = num("Tipo evento: ", 1, 3);
                TipoEvento tipoEvento = (te == 1) ? TipoEvento.CONFERENCIA : (te == 2 ? TipoEvento.TALLER : TipoEvento.REUNION);
                String desc = txt("Descripción: ", true);
                r = new ReservaEventos(id, aula, f, hi, hf, resp, EstadoReserva.ACTIVA, tipoEvento, desc);
            } else {
                String lab = txt("Laboratorio: ", true);
                String s = txt("¿Requiere asistente? (S/N): ", true);
                boolean asist = s.equalsIgnoreCase("S");
                r = new ReservaPractica(id, aula, f, hi, hf, resp, EstadoReserva.ACTIVA, lab, asist);
            }

            r.validar();
            DataStore.verificarConflicto(r);
            DataStore.RESERVAS.add(r);
            DataStore.saveReservas();
            System.out.println("Reserva creada. ID: " + r.getId());
        } catch (ValidacionException e) {
            System.out.println("Validación: " + e.getMessage());
        } catch (ConflictoReservaException e) {
            System.out.println("Conflicto: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pausa();
    }

private static void listarBuscarOrdenar() {
    DataStore.actualizarHistoricas();

    // Solo buscar por nombre del responsable
    String filtroResp = txt("Buscar por nombre del responsable (enter para ver todas): ", false);
    final String fr = (filtroResp == null) ? "" : filtroResp.trim().toLowerCase();

    // Aplica ÚNICAMENTE ese filtro
    List<Reserva> lista = DataStore.RESERVAS.stream()
            .filter(r -> fr.isEmpty() || r.getResponsable().toLowerCase().contains(fr))
            .collect(Collectors.toList());

    // Ordenamiento (igual que antes)
    System.out.println("\nOrden: 1=Fecha↑  2=Fecha↓  3=Aula  4=Duración  0=Sin ordenar");
    int ord = num("Opción: ", 0, 4);
    switch (ord) {
        case 1:
            java.util.Collections.sort(lista,
                    Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getInicio));
            break;
        case 2:
            java.util.Collections.sort(lista,
                    Comparator.comparing(Reserva::getFecha).reversed().thenComparing(Reserva::getInicio).reversed());
            break;
        case 3:
            java.util.Collections.sort(lista,
                    Comparator.comparing(Reserva::getAulaCodigo).thenComparing(Reserva::getFecha));
            break;
        case 4:
            java.util.Collections.sort(lista,
                    Comparator.comparingLong(Reserva::duracionMin).reversed());
            break;
        default:
            // sin ordenar
    }

    if (lista.isEmpty()) {
        System.out.println("No hay resultados.");
        pausa();
        return;
    }

    System.out.println("\nID                                  | Fecha      | Tipo     | Inicio | Fin   | Aula | Responsable  | Estado");
    System.out.println("-------------------------------------------------------------------------------------------------------------");
    for (Reserva r : lista) {
        System.out.printf("%-35s | %-10s | %-8s | %-6s | %-5s | %-4s | %-13s | %-9s\n",
                r.getId(), r.getFecha(), r.tipo(), r.getInicio(), r.getFin(),
                r.getAulaCodigo(), trunc(r.getResponsable(), 13), r.getEstado());
    }
    pausa();
}


    private static void modificarReserva() {
        try {
            String id = txt("ID de la reserva a modificar: ", true);
            Reserva r = DataStore.RESERVAS.stream()
                    .filter(x -> x.getId().equals(id))
                    .findFirst()
                    .orElseThrow(new java.util.function.Supplier<RecursoNoEncontradoException>() {
                        @Override public RecursoNoEncontradoException get() {
                            return new RecursoNoEncontradoException("No existe ese ID.");
                        }
                    });

            String s = txt("Nueva fecha (yyyy-MM-dd, enter mantiene): ", false);
            if (s != null && !s.trim().isEmpty()) r.setFecha(LocalDate.parse(s.trim(), DataStore.F_FECHA));

            s = txt("Nueva hora inicio (HH:mm, enter mantiene): ", false);
            if (s != null && !s.trim().isEmpty()) r.setInicio(LocalTime.parse(s.trim(), DataStore.F_HORA));

            s = txt("Nueva hora fin (HH:mm, enter mantiene): ", false);
            if (s != null && !s.trim().isEmpty()) r.setFin(LocalTime.parse(s.trim(), DataStore.F_HORA));

            s = txt("Nuevo responsable (enter mantiene): ", false);
            if (s != null && !s.trim().isEmpty()) r.setResponsable(s.trim());

            // Campos específicos
            if (r instanceof ReservaClase) {
                ReservaClase rc = (ReservaClase) r;
                s = txt("Materia (enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty()) rc.setMateria(s.trim());
                s = txt("Grupo (enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty()) rc.setGrupo(s.trim());
            } else if (r instanceof ReservaEventos) {
                ReservaEventos re = (ReservaEventos) r;
                s = txt("TipoEvento [CONFERENCIA/TALLER/REUNION] (enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty())
                    re.setTipoEvento(TipoEvento.valueOf(s.trim().toUpperCase()));
                s = txt("Descripción (enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty()) re.setDescripcion(s.trim());
            } else if (r instanceof ReservaPractica) {
                ReservaPractica rp = (ReservaPractica) r;
                s = txt("Laboratorio (enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty()) rp.setLaboratorio(s.trim());
                s = txt("¿Requiere asistente? (S/N, enter mantiene): ", false);
                if (s != null && !s.trim().isEmpty()) rp.setAsistente(s.trim().equalsIgnoreCase("S"));
            }

            // Validar y verificar conflictos con otras reservas activas
            r.validar();
            boolean conflicto = DataStore.RESERVAS.stream()
                    .filter(x -> !x.getId().equals(r.getId()))
                    .filter(x -> x.getEstado() == EstadoReserva.ACTIVA)
                    .filter(x -> x.getAulaCodigo().equalsIgnoreCase(r.getAulaCodigo()))
                    .filter(x -> x.getFecha().equals(r.getFecha()))
                    .anyMatch(x -> DataStore.overlap(x.getInicio(), x.getFin(), r.getInicio(), r.getFin()));

            if (conflicto) throw new ConflictoReservaException("La modificación genera conflicto con otra reserva.");

            DataStore.saveReservas();
            System.out.println("Reserva modificada.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pausa();
    }

    private static void cancelarReserva() {
        try {
            String id = txt("ID de la reserva a cancelar: ", true);
            Reserva r = DataStore.RESERVAS.stream()
                    .filter(x -> x.getId().equals(id))
                    .findFirst()
                    .orElseThrow(new java.util.function.Supplier<RecursoNoEncontradoException>() {
                        @Override public RecursoNoEncontradoException get() {
                            return new RecursoNoEncontradoException("No existe ese ID.");
                        }
                    });

            if (r.getEstado() == EstadoReserva.CANCELADA) {
                System.out.println("La reserva ya está cancelada.");
            } else {
                r.setEstado(EstadoReserva.CANCELADA);
                DataStore.saveReservas();
                System.out.println("Reserva cancelada.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        pausa();
    }

    // ------------------- Menú Reportes -------------------
    private static void menuReportes() {
        while (true) {
            System.out.println("\n-- Reportes --");
            System.out.println("1) Top 3 aulas por horas");
            System.out.println("2) Ocupación por tipo de aula");
            System.out.println("3) Distribución por tipo de reserva");
            System.out.println("4) Exportar resumen TXT");
            System.out.println("0) Volver");
            int op = num("Opción: ", 0, 4);
            switch (op) {
                case 1:
                    reporteTop3();
                    break;
                case 2:
                    reporteOcupacion();
                    break;
                case 3:
                    reporteDistribucion();
                    break;
                case 4:
                    DataStore.exportarTXT();
                    System.out.println("Reporte exportado en: " + DataStore.FILE_REPORTE);
                    pausa();
                    break;
                default:
                    return;
            }
        }
    }

    private static void reporteTop3() {
        List<java.util.Map.Entry<String, Long>> top = DataStore.minutosPorAulaSinCanceladas().entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        int i = 1;
        System.out.println("\nTop 3 aulas por horas:");
        for (java.util.Map.Entry<String, Long> e : top) {
            String nombre = DataStore.AULAS.stream()
                    .filter(a -> a.getCodigo().equalsIgnoreCase(e.getKey()))
                    .map(Aula::getNombre)
                    .findFirst()
                    .orElse("N/D");
            System.out.printf("%d) %s (%s) - %.2f h\n", i++, nombre, e.getKey(), e.getValue() / 60.0);
        }
        pausa();
    }

    private static void reporteOcupacion() {
        System.out.println("\nOcupación por tipo de aula (horas):");
        for (java.util.Map.Entry<TipoAula, Long> e : DataStore.ocupacionPorTipo().entrySet()) {
            System.out.printf("- %-11s: %.2f\n", e.getKey(), e.getValue() / 60.0);
        }
        pausa();
    }

    private static void reporteDistribucion() {
        System.out.println("\nDistribución por tipo de reserva (conteo):");
        for (java.util.Map.Entry<String, Long> e : DataStore.distPorTipoReserva().entrySet()) {
            System.out.printf("- %-8s: %d\n", e.getKey(), e.getValue());
        }
        pausa();
    }

    // ------------------- Menú principal -------------------
    private static void menuPrincipal() {
        while (true) {
            System.out.println("\n== Gestor de Reservas ITCA ==");
            System.out.println("1) Gestión de Aulas");
            System.out.println("2) Gestión de Reservas");
            System.out.println("3) Reportes");
            System.out.println("9) Guardar todo");
            System.out.println("0) Salir");
            int op = num("Opción: ", 0, 9);
            switch (op) {
                case 1:
                    menuAulas();
                    break;
                case 2:
                    menuReservas();
                    break;
                case 3:
                    menuReportes();
                    break;
                case 9:
                    DataStore.saveAulas();
                    DataStore.saveReservas();
                    System.out.println("Datos guardados.");
                    pausa();
                    break;
                default:
                    System.out.println("¡Hasta pronto!");
                    return;
            }
        }
    }

    public static void main(String[] args) {
        DataStore.initFiles();
        DataStore.load();
        menuPrincipal();
    }
}

