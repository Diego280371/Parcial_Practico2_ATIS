package itca;

import itca.Validaciones.ValidacionException;

public class ReservaPractica extends Reserva {
    private String laboratorio; private boolean asistente;

    public ReservaPractica(String id, String aula, java.time.LocalDate f, java.time.LocalTime i, java.time.LocalTime fn,
                           String resp, EstadoReserva est, String lab, boolean asist) {
        super(id, aula, f, i, fn, resp, est);
        this.laboratorio = lab; this.asistente = asist;
    }

    public String getLaboratorio(){return laboratorio;}
    public boolean isAsistente(){return asistente;}
    public void setLaboratorio(String l){laboratorio=l;}
    public void setAsistente(boolean a){asistente=a;}

    @Override public String tipo(){ return "PRACTICA"; }

    @Override public void validar() throws ValidacionException {
        super.validar();
        if (laboratorio == null || laboratorio.trim().isEmpty()) throw new ValidacionException("Laboratorio requerido.");
        Aula a = DataStore.buscarAula(aulaCodigo);
        if (a.getTipo() != TipoAula.LABORATORIO) throw new ValidacionException("Práctica solo en LABORATORIO.");
    }

    @Override public String toCsvExtra(){ return laboratorio + ";" + asistente; }
}
