package itca;

import java.time.LocalDate;
import java.time.LocalTime;
import itca.Validaciones.ValidacionException;

public abstract class Reserva implements Validable {
    protected String id, aulaCodigo, responsable;
    protected LocalDate fecha;
    protected LocalTime inicio, fin;
    protected EstadoReserva estado;

    protected Reserva(String id, String aulaCodigo, LocalDate fecha, LocalTime inicio, LocalTime fin, String responsable, EstadoReserva estado) {
        this.id = id; this.aulaCodigo = aulaCodigo; this.fecha = fecha; this.inicio = inicio; this.fin = fin; this.responsable = responsable; this.estado = estado;
    }

    public String getId(){return id;}
    public String getAulaCodigo(){return aulaCodigo;}
    public LocalDate getFecha(){return fecha;}
    public LocalTime getInicio(){return inicio;}
    public LocalTime getFin(){return fin;}
    public String getResponsable(){return responsable;}
    public EstadoReserva getEstado(){return estado;}
    public void setFecha(LocalDate f){fecha=f;}
    public void setInicio(LocalTime t){inicio=t;}
    public void setFin(LocalTime t){fin=t;}
    public void setResponsable(String r){responsable=r;}
    public void setEstado(EstadoReserva e){estado=e;}

    public long duracionMin(){ return java.time.Duration.between(inicio, fin).toMinutes(); }
    public abstract String tipo();
    public abstract String toCsvExtra();

    public String toCsv(){
        return String.join(";", id, aulaCodigo, fecha.format(DataStore.F_FECHA),
                inicio.format(DataStore.F_HORA), fin.format(DataStore.F_HORA),
                responsable, estado.name(), tipo(), toCsvExtra());
    }

    @Override
    public void validar() throws ValidacionException {
        if (aulaCodigo == null || aulaCodigo.trim().isEmpty()) throw new ValidacionException("Aula requerido.");
        if (fecha == null) throw new ValidacionException("Fecha requerida.");
        if (inicio == null || fin == null) throw new ValidacionException("Horas requeridas.");
        if (!fin.isAfter(inicio)) throw new ValidacionException("Fin > Inicio.");
        if (responsable == null || responsable.trim().isEmpty()) throw new ValidacionException("Responsable requerido.");

        LocalTime min = LocalTime.of(6, 0);
        LocalTime max = LocalTime.of(22, 0);
        if (inicio.isBefore(min) || fin.isAfter(max)) throw new ValidacionException("Horario permitido 06:00–22:00.");
        if (inicio.getMinute() % 30 != 0 || fin.getMinute() % 30 != 0) throw new ValidacionException("Intervalos de 30 minutos.");
    }

    public static Reserva fromCsv(String line) {
        String[] p = line.split(";", -1);
        String id = p[0];
        String aula = p[1];
        LocalDate fecha = LocalDate.parse(p[2], DataStore.F_FECHA);
        LocalTime hi = LocalTime.parse(p[3], DataStore.F_HORA);
        LocalTime hf = LocalTime.parse(p[4], DataStore.F_HORA);
        String resp = p[5];
        EstadoReserva estado = EstadoReserva.valueOf(p[6]);
        String tipo = p[7];

        if ("CLASE".equals(tipo)) {
            return new ReservaClase(id, aula, fecha, hi, hf, resp, estado, p[8], p[9]);
        } else if ("EVENTO".equals(tipo)) {
            return new ReservaEventos(id, aula, fecha, hi, hf, resp, estado, TipoEvento.valueOf(p[8]), p[9]);
        } else { // PRACTICA
            return new ReservaPractica(id, aula, fecha, hi, hf, resp, estado, p[8], Boolean.parseBoolean(p[9]));
        }
    }
}

