/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.excepciones;

/**
 * @author segundo.sagnay
 */
public class FirmaElectronicaException extends Exception {

    private static final long serialVersionUID = 6019098173538127532L;

    public FirmaElectronicaException(String msg) {
        super(msg);
    }

    public FirmaElectronicaException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public FirmaElectronicaException(Throwable ex) {
        super(ex);
    }
}