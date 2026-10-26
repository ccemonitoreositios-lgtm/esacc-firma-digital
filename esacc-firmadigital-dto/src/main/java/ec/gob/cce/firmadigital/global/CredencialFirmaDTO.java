/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global;

import org.jboss.resteasy.annotations.jaxrs.FormParam;

/**
 * DTO para manejo de credenciales de firma electrónica
 * 
 * @author segundo.sagnay
 */
public class CredencialFirmaDTO {

    private String usuario;
    @FormParam("contrasenia")
    private String contrasenia;
    private String pin;

    @FormParam("certificado")
    private byte[] certificado;

    private String datosAdicionales;
    
    public Long expiryTime;
    
    private String certificadoBase64;

    public CredencialFirmaDTO() {
        super();
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public char[] getPassword() {
        return contrasenia.toCharArray();
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public byte[] getCertificado() {
        return certificado;
    }

    public void setCertificado(byte[] certificado) {
        this.certificado = certificado;
    }

    public String getDatosAdicionales() {
        return datosAdicionales;
    }

    public void setDatosAdicionales(String datosAdicionales) {
        this.datosAdicionales = datosAdicionales;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getCertificadoBase64() {
        return certificadoBase64;
    }

    public void setCertificadoBase64(String certificadoBase64) {
        this.certificadoBase64 = certificadoBase64;
    }
    
    
    
    
}
