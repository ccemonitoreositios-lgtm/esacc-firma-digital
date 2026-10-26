/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import ec.gob.cce.firmadigital.global.DocumentoDTO;
import ec.gob.cce.firmadigital.global.FirmaElectronicaRest;
import ec.gob.cce.firmadigital.global.ResponseDTO;
import ec.gob.cce.firmadigital.global.RespuestaArchivoDTO;
import ec.gob.cce.global.JsonUtil;
import ec.gob.cce.global.enumerados.TipoMensaje;
import ec.gob.firmadigital.global.servicio.ServicioFirmaDigital;
import ec.gob.firmadigital.libreria.exceptions.SignatureVerificationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class FirmaElectronicaRestImpl implements FirmaElectronicaRest {

    private final Logger log = Logger.getLogger(this.getClass());

    @Inject
    private ServicioFirmaDigital servicioFirmaDigital;

    private final JsonUtil jsonUtil = new JsonUtil();

   
   

    @Override
    public Response validarFirmaPorUuid(String param) {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
        DocumentoDTO documento = null;
        try {
            documento = this.jsonUtil.jsonToClass(param, DocumentoDTO.class);
            respuesta = servicioFirmaDigital.verificarFirmaElectronicaPorUuid(documento);
            return Response.status(Response.Status.OK).entity(respuesta).build();

        } catch (UnsupportedEncodingException e) {
            log.error("Error al procesar el documento", e);           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        } catch (IOException e) {
            log.error("Error de entrada del documento", e);           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        } catch (SignatureVerificationException e) {
            log.error("Error al verificar el documento", e);           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        } catch (Exception e) {
            log.error("Error general del documento", e);           
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        }

    }

    
   

 
    
    
    @Override
    public Response firmarDocumentoProcesoFirmaMasiva(DocumentoDTO documento) {
        ResponseDTO<RespuestaArchivoDTO> respuesta = new ResponseDTO<>();
        try {
            respuesta = servicioFirmaDigital.firmarDocumentoProcesoFirmaMasiva(documento);
            return Response.status(Response.Status.OK).entity(respuesta).build();
        } catch (Exception e) {
            log.error("Error en el proceso de firma electrónica", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        }

    }

    @Override
    public Response obtenerFechaSistema() {
        final ResponseDTO<Date> respuesta = new ResponseDTO<>();
        try {
            final long ONE_HOUR_IN_MILLIS = 1 * 60 * 60 * 1000L;
            
            final Calendar date = Calendar.getInstance();
            final long t = date.getTimeInMillis();
            
            final Date fecha = new Date(t + ONE_HOUR_IN_MILLIS);
            
            respuesta.setDato(fecha);
            respuesta.setTipoMensaje(TipoMensaje.EXITO.getTipo());
            respuesta.setMensaje("");
            return Response.status(Response.Status.OK).entity(respuesta).build();
        } catch (final Exception e) {
            respuesta.setMensaje("Los datos de entrada no son válidos");
            respuesta.setTipoMensaje(TipoMensaje.ERROR.getTipo());
            if (null != e.getCause()) {
                this.log.error(e.getMessage(), e);
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        }
    }

    @Override
    public Response verificarFirmaElectronicaSacc(DocumentoDTO documento) {

        try {
            ResponseDTO<Boolean> respuesta =
                    servicioFirmaDigital.verificarFirmaElectronicaSacc(documento);

            return Response.status(Response.Status.OK)
                           .entity(respuesta)
                           .build();

        } catch (Exception e) {
            log.error("Error al verificar la firma electrónica", e);

            ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(respuesta)
                           .build();
        }
    }
    
    @Override
    public Response verificarFirmaElectronicaProcesoFirmaMasiva(DocumentoDTO documento) {

        try {
            ResponseDTO<Boolean> respuesta =
                    servicioFirmaDigital.verificarFirmaElectronicaProcesoFirmaMasiva(documento);

            return Response.ok(respuesta).build();

        } catch (Exception e) {
            log.error("Error al verificar firma electrónica en proceso masivo", e);

            ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(respuesta)
                           .build();
        }
    }
      
    
    @Override
    public Response verificarFirmaElectronicaSaccBase64(String param) {

        try {
            DocumentoDTO documento = jsonUtil.jsonToClass(param, DocumentoDTO.class);

            ResponseDTO<Boolean> respuesta =
                    servicioFirmaDigital.verificarFirmaElectronicaSaccBase64(documento);

            return Response.ok(respuesta).build();

        } catch (Exception e) {
            log.error("Error al verificar firma electrónica Base64", e);

            ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(respuesta)
                           .build();
        }
    }

}