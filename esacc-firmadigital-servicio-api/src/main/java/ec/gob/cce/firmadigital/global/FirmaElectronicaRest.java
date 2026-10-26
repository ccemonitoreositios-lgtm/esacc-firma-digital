/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
@Path("/firmadigital")
@Produces(MediaType.APPLICATION_JSON)
public interface FirmaElectronicaRest {

 
    /**
     * Permite firmar un documento para el proceso masivo.
     *
     * @author Administrador
     * @history 4 abr. 2025 - 10:38:37 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    
    @POST
    @Path("/firmarDocumentoProcesoFirmaMasiva")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response firmarDocumentoProcesoFirmaMasiva(@MultipartForm DocumentoDTO documento);
    
    
    
    /**
     * Permite validar un documento subido a Hadoop
     *
     * @author Administrador
     * @history 1 abr. 2025 - 12:31:27 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/validarFirmaPorUuid")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validarFirmaPorUuid(String param);
    
    
    /**
     * Permite Obtener la fecha del servido sumado una hora
     *
     * @author Administrador
     * @history 4 abr. 2025 - 11:43:14 Administrador
     * Versión inicial.
     * @return
     */
    @GET
    @Path("fechaSistema")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response obtenerFechaSistema();
    
    /**
     * Permite verificar la validez de un documento firmado electronicamente
     *
     * @author Administrador
     * @history 7 abr. 2025 - 14:32:32 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/verificarFirmaElectronicaSacc")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response verificarFirmaElectronicaSacc(@MultipartForm DocumentoDTO documento);
    
    
    /**
     * Permite verificar un documento que tenga firmas validas o el docuemento no contenga firmas 
     *
     * @author Administrador
     * @history 8 abr. 2025 - 16:44:15 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/verificarFirmaElectronicaProcesoFirmaMasiva")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response verificarFirmaElectronicaProcesoFirmaMasiva(@MultipartForm DocumentoDTO documento);
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 8 ene. 2026 - 15:14:59 Administrador
     * Versión inicial.
     * @param param
     * @return
     */
    @POST
    @Path("/verificarFirmaElectronicaSaccBase64")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verificarFirmaElectronicaSaccBase64(String param);
}
