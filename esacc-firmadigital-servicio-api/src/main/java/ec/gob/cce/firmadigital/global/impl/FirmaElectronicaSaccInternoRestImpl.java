/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global.impl;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import ec.gob.cce.firmadigital.global.CredencialFirmaDTO;
import ec.gob.cce.firmadigital.global.DocumentoDTO;
import ec.gob.cce.firmadigital.global.FirmaElectronicaSaccInternoRest;
import ec.gob.cce.firmadigital.global.ResponseDTO;
import ec.gob.cce.global.JsonUtil;
import ec.gob.firmadigital.global.servicio.ServicioFirmaDigitalV2;
import ec.gob.firmadigital.libreria.certificate.to.Certificado;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class FirmaElectronicaSaccInternoRestImpl implements FirmaElectronicaSaccInternoRest {

    private final Logger log = Logger.getLogger(this.getClass());

    @Inject
    private ServicioFirmaDigitalV2 servicioFirmaDigitalV2;

    private final JsonUtil jsonUtil = new JsonUtil();

   
   
    
    @Override
    public Response obtenerCredencialesFirma(String param) {
        ResponseDTO<CredencialFirmaDTO> respuesta = new ResponseDTO<>();
        try {
           
            final JsonObject data = this.jsonUtil.getJson(param);
            final String  mensaje = data.get("mensaje").getAsString();
            final String certificadoString = data.get("certificado").getAsString();
            byte[] certificadoBytes = Base64.getDecoder().decode(certificadoString);
            
            CredencialFirmaDTO resp = servicioFirmaDigitalV2.obtenerCredencialesFirma(mensaje, certificadoBytes);            
            respuesta.setDato(resp);
            return Response.status(Response.Status.OK).entity(respuesta).build();

        } catch (UnsupportedEncodingException e) {
            log.error("Error al obtener Credenciales  firma electrónica", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        }  catch (Exception e) {
            log.error("Error general al obtener Credenciales  firma electrónica", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();
        }

    }
    
    
    @Override
    public Response validarCredenciales(String param) {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();

        try {

            CredencialFirmaDTO credencial = this.jsonUtil.jsonToClass(param, CredencialFirmaDTO.class);

            // ✅ Validación defensiva
            if (credencial.getCertificadoBase64() != null && !credencial.getCertificadoBase64().isEmpty()) {
                byte[] certBytes = Base64.getDecoder().decode(credencial.getCertificadoBase64());
                credencial.setCertificado(certBytes);
            } else {
                throw new RuntimeException("Certificado vacío o nulo");
            }

            Boolean resp = servicioFirmaDigitalV2.validarCredenciales(credencial);
            respuesta.setDato(resp);

            return Response.status(Response.Status.OK).entity(respuesta).build();

        } catch (IllegalArgumentException e) {
            // 👇 Error típico si el Base64 viene mal
            log.error("Error al validar Credenciales firma electrónica", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Certificado en formato inválido (Base64)")
                    .build();

        } catch (UnsupportedEncodingException e) {
            log.error("Error al eoncoding firma electrónica", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(respuesta).build();

        } catch (Exception e) {
            log.error("Error al general", e);

            respuesta.setMensaje(e.getMessage()); // 👈 IMPORTANTE
            respuesta.setDato(false);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(respuesta)
                    .build();
        }
    }


   
    
    @Override
    public Response firmarArchivo(DocumentoDTO documento) {

        try {

            CredencialFirmaDTO credencial = new CredencialFirmaDTO();
            credencial.setUsuario(documento.getUsuario());
            credencial.setContrasenia(documento.getContrasenia());
            credencial.setPin(documento.getPin());

            credencial.setCertificado(documento.getCertificado());

            if (documento.getFile() == null || documento.getFile().length == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Archivo vacío o no enviado").build();
            }

            if (documento.getCertificado() == null || documento.getCertificado().length == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Certificado vacío o no enviado").build();
            }

            if (documento.getNumeroFirma() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Número de firma requerido").build();
            }

            if (documento.getModulo() == null || documento.getModulo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Módulo requerido").build();
            }

            byte[] firmado = this.servicioFirmaDigitalV2.firmarArchivo(credencial, documento.getFile(), documento.getNumeroFirma(), documento.getModulo());

            if (firmado == null || firmado.length == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo generar el archivo firmado").build();
            }

            
            return Response.ok(firmado).header("Content-Disposition", "attachment; filename=archivo_firmado.pdf").build();

        } catch (ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException e) {

            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            log.error("Error  general firmar Archivo", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al firmar archivo").build();
        }
    }
    
    @Override
    public Response verificarDocumento(DocumentoDTO documento) {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
        try {

            if (documento.getFile() == null || documento.getFile().length == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Archivo vacío o no enviado").build();
            }

            Boolean valido = this.servicioFirmaDigitalV2.verificarDocumento(documento.getFile());
            respuesta.setDato(valido);
            return Response.status(Response.Status.OK).entity(respuesta).build();

        } catch (ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException e) {

            // 👇 Error funcional (ej: contraseña incorrecta)
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            log.error("Error  general", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al firmar archivo").build();
        }
    }
    
    
    @Override
    public Response pdfToCertificados(DocumentoDTO documento) {
        ResponseDTO<List<Certificado>> respuesta = new ResponseDTO<>();
        try {

            if (documento.getFile() == null || documento.getFile().length == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Archivo vacío o no enviado").build();
            }

            List<Certificado> certificados = this.servicioFirmaDigitalV2.pdfToCertificados(documento.getFile());
            respuesta.setDato(certificados);
            return Response.status(Response.Status.OK).entity(respuesta).build();

        } catch (ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException e) {

            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            log.error("Error  general", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al firmar archivo").build();
        }
    }

}