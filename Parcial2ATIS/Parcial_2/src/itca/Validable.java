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

import itca.Validaciones.ValidacionException;

public interface Validable {
    void validar() throws ValidacionException;
}

