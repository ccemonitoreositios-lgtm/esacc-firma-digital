/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.global.enumerados;

/**
 * Enumerados para tipos de mensajes
 * @author segundo.sagnay
 */
public enum TipoMensaje {
    ERROR("error"),
    ADVERTENCIA("advertencia"),
    EXITO("exito"),
    INFORMACION("informacion");

    private String tipo;

    private TipoMensaje(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return this.tipo;
    }

    @Override
    public String toString() {
        return this.tipo;
    }
}
