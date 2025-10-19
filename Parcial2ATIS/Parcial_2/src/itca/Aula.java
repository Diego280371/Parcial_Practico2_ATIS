/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itca;

import itca.TipoAula;
import itca.Validaciones.ValidacionException;

/**
 *
 * @author Diego Mejia
 */
public class Aula implements Validable{
    private String codigo, nombre;
    private TipoAula tipo;
    private int capacidad;
    
    public Aula(String codigo, String nombre, TipoAula tipo, int capacidad){
        this.codigo = codigo; this.nombre = nombre; this.tipo = tipo; this.capacidad = capacidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoAula getTipo() {
        return tipo;
    }

    public void setTipo(TipoAula tipo) {
        this.tipo = tipo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }


@Override public void validar() throws ValidacionException{
    if(codigo == null || " ".equals(codigo)) throw new ValidacionException("Codigo requerido");
    if(nombre == null || " ".equals(nombre)) throw new ValidacionException("Nombre requerido");
    if(tipo == null) throw new ValidacionException("Tipo requerido");
    if(capacidad <= 0) throw new ValidacionException("Tiene que tener una capacidad");
}

public String toCVS(){return String.join(";", codigo, nombre, tipo.name(), String.valueOf(capacidad));}
 public static Aula fromCsv(String line){
        String[] p=line.split(";",-1);
        return new Aula(p[0], p[1], TipoAula.valueOf(p[2]), Integer.parseInt(p[3]));
    }
    @Override public String toString(){
        return String.format("%-8s | %-18s | %-11s | %4d", codigo, nombre, tipo, capacidad);
    }
}
