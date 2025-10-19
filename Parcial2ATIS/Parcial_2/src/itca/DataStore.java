package itca;

import itca.Validaciones.ConflictoReservaException;
import itca.Validaciones.ValidacionException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths; // Java 8: usar Paths.get(...)
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataStore {
    public static final String FILE_AULAS    = "aulas.csv";
    public static final String FILE_RESERVAS = "reservas.csv";
    public static final String FILE_REPORTE  = "reporte_resumen.txt";

    public static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter F_HORA  = DateTimeFormatter.ofPattern("HH:mm");

    public static final List<Aula> AULAS       = new ArrayList<>();
    public static final List<Reserva> RESERVAS = new ArrayList<>();

    // Crea los archivos si no existen (Java 8)
    public static void initFiles() {
        try {
            if (!Files.exists(Paths.get(FILE_AULAS))) {
                Files.createFile(Paths.get(FILE_AULAS));
            }
            if (!Files.exists(Paths.get(FILE_RESERVAS))) {
                Files.createFile(Paths.get(FILE_RESERVAS));
            }
        } catch (IOException ignored) { }
    }

    // Carga CSV a memoria (Java 8: sin isBlank)
    public static void load() {
        // Aulas
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_AULAS))) {
            AULAS.clear();
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln != null && !ln.trim().isEmpty()) {
                    AULAS.add(Aula.fromCsv(ln));
                }
            }
        } catch (Exception ignored) { }

        // Reservas
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_RESERVAS))) {
            RESERVAS.clear();
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln != null && !ln.trim().isEmpty()) {
                    RESERVAS.add(Reserva.fromCsv(ln));
                }
            }
        } catch (Exception ignored) { }

        actualizarHistoricas();
    }

    // Guarda aulas a CSV
    public static void saveAulas() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_AULAS))) {
            for (Aula a : AULAS) {
                bw.write(a.toCVS() + "\n");
            }
        } catch (Exception e) {
            System.out.println("Error guardando aulas: " + e.getMessage());
        }
    }

    // Guarda reservas a CSV
    public static void saveReservas() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_RESERVAS))) {
            for (Reserva r : RESERVAS) {
                bw.write(r.toCsv() + "\n");
            }
        } catch (Exception e) {
            System.out.println("Error guardando reservas: " + e.getMessage());
        }
    }

    // Buscar aula por código (lanza excepción propia)
    public static Aula buscarAula(String codigo) throws ValidacionException {
        return AULAS.stream()
                .filter(a -> a.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(() -> new ValidacionException("Aula no existe: " + codigo));
    }

    // Verifica solapamiento de horarios
    public static void verificarConflicto(Reserva n) throws ConflictoReservaException {
        boolean conf = RESERVAS.stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .filter(r -> r.getAulaCodigo().equalsIgnoreCase(n.getAulaCodigo()))
                .filter(r -> r.getFecha().equals(n.getFecha()))
                .anyMatch(r -> overlap(r.getInicio(), r.getFin(), n.getInicio(), n.getFin()));
        if (conf) throw new ConflictoReservaException("Conflicto de horario en el aula/fecha indicada.");
    }

    public static boolean overlap(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {
        return !a2.equals(b1) && !b2.equals(a1) && a1.isBefore(b2) && b1.isBefore(a2);
    }

    // Pasa ACTIVA -> HISTORICA si la fecha ya pasó
    public static void actualizarHistoricas() {
        LocalDate hoy = LocalDate.now();
        for (Reserva r : RESERVAS) {
            if (r.getEstado() == EstadoReserva.ACTIVA && r.getFecha().isBefore(hoy)) {
                r.setEstado(EstadoReserva.HISTORICA);
            }
        }
    }

    // -------- Reportes (Streams compatibles con Java 8) --------

    public static Map<String, Long> minutosPorAulaSinCanceladas() {
        return RESERVAS.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .collect(Collectors.groupingBy(
                        Reserva::getAulaCodigo,
                        Collectors.summingLong(Reserva::duracionMin)
                ));
    }

    public static Map<TipoAula, Long> ocupacionPorTipo() {
        Map<TipoAula, Long> m = new EnumMap<>(TipoAula.class);
        for (TipoAula t : TipoAula.values()) m.put(t, 0L);

        RESERVAS.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .forEach(r -> {
                    try {
                        Aula a = buscarAula(r.getAulaCodigo());
                        m.put(a.getTipo(), m.get(a.getTipo()) + r.duracionMin());
                    } catch (Exception ignored) { }
                });
        return m;
    }

    public static Map<String, Long> distPorTipoReserva() {
        return RESERVAS.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .collect(Collectors.groupingBy(Reserva::tipo, Collectors.counting()));
    }

    // Exporta resumen a TXT
    public static void exportarTXT() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_REPORTE))) {
            // Top 3 aulas por minutos
            List<Map.Entry<String, Long>> top = minutosPorAulaSinCanceladas().entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            bw.write("== Top 3 aulas por horas ==\n");
            int i = 1;
            for (Map.Entry<String, Long> e : top) {
                String nombre = AULAS.stream()
                        .filter(a -> a.getCodigo().equalsIgnoreCase(e.getKey()))
                        .map(Aula::getNombre)
                        .findFirst()
                        .orElse("N/D");
                bw.write(String.format("%d) %s (%s) - %.2f horas\n",
                        i++, nombre, e.getKey(), e.getValue() / 60.0));
            }

            bw.write("\n== Ocupación por tipo de aula (h) ==\n");
            for (Map.Entry<TipoAula, Long> e : ocupacionPorTipo().entrySet()) {
                bw.write(String.format("- %-11s: %.2f\n", e.getKey(), e.getValue() / 60.0));
            }

            bw.write("\n== Distribución por tipo de reserva ==\n");
            for (Map.Entry<String, Long> e : distPorTipoReserva().entrySet()) {
                bw.write(String.format("- %-8s: %d\n", e.getKey(), e.getValue()));
            }
        } catch (Exception e) {
            System.out.println("Error exportando: " + e.getMessage());
        }
    }
}
