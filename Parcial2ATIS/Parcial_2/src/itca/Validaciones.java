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
public class Validaciones {

    // Excepción para validaciones generales
    public static class ValidacionException extends Exception {
        public ValidacionException(String mensaje) {
            super(mensaje);
        }
    }

    // Excepción para conflictos de reserva
    public static class ConflictoReservaException extends Exception {
        public ConflictoReservaException(String mensaje) {
            super(mensaje);
        }
    }

    // Excepción cuando no se encuentra un recurso
    public static class RecursoNoEncontradoException extends Exception {
        public RecursoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}