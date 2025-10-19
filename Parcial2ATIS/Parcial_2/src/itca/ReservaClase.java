package itca;

import itca.Validaciones.ValidacionException;

public class ReservaClase extends Reserva {
    private String materia, grupo;

    public ReservaClase(String id, String aula, java.time.LocalDate f, java.time.LocalTime i, java.time.LocalTime fn,
                        String resp, EstadoReserva est, String materia, String grupo) {
        super(id, aula, f, i, fn, resp, est);
        this.materia = materia; this.grupo = grupo;
    }

    public String getMateria(){return materia;}
    public String getGrupo(){return grupo;}
    public void setMateria(String m){materia=m;}
    public void setGrupo(String g){grupo=g;}

    @Override public String tipo(){ return "CLASE"; }

    @Override public void validar() throws ValidacionException {
        super.validar();
        if (materia == null || materia.trim().isEmpty()) throw new ValidacionException("Materia requerida.");
        if (grupo == null || grupo.trim().isEmpty()) throw new ValidacionException("Grupo requerido.");
        Aula a = DataStore.buscarAula(aulaCodigo);
        if (a.getTipo() == TipoAula.AUDITORIO) throw new ValidacionException("Clase no puede ser en AUDITORIO.");
    }

    @Override public String toCsvExtra(){ return materia + ";" + grupo; }
}
