package itca;

import itca.Validaciones.ValidacionException;

public class ReservaEventos extends Reserva {
    private TipoEvento tipoEvento; private String descripcion;

    public ReservaEventos(String id, String aula, java.time.LocalDate f, java.time.LocalTime i, java.time.LocalTime fn,
                         String resp, EstadoReserva est, TipoEvento te, String desc) {
        super(id, aula, f, i, fn, resp, est);
        this.tipoEvento = te; this.descripcion = desc;
    }

    public TipoEvento getTipoEvento(){return tipoEvento;}
    public String getDescripcion(){return descripcion;}
    public void setTipoEvento(TipoEvento t){tipoEvento=t;}
    public void setDescripcion(String d){descripcion=d;}

    @Override public String tipo(){ return "EVENTO"; }

    @Override public void validar() throws ValidacionException {
        super.validar();
        if (tipoEvento == null) throw new ValidacionException("Tipo de evento requerido.");
        if (descripcion == null || descripcion.trim().isEmpty()) throw new ValidacionException("Descripción requerida.");
        Aula a = DataStore.buscarAula(aulaCodigo);
        if (a.getTipo() == TipoAula.LABORATORIO) throw new ValidacionException("Evento no puede ser en LABORATORIO.");
    }

    @Override public String toCsvExtra(){ return tipoEvento.name() + ";" + descripcion; }
}
