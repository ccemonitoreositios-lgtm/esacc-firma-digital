/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
@Path("/firmaDigitalSaccInterno")
@Produces(MediaType.APPLICATION_JSON)
public interface FirmaElectronicaSaccInternoRest {

 
    
    /**
     * 
     *
     * @author Administrador
     * @history 17 mar. 2026 - 09:16:59 Administrador
     * Versión inicial.
     * @param param
     * @return
     */
    @POST
    @Path("/obtenerCredencialesFirma")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response obtenerCredencialesFirma(String param);
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 17 mar. 2026 - 11:16:25 Administrador
     * Versión inicial.
     * @param param
     * @return
     */
    @POST
    @Path("/validarCredenciales")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validarCredenciales(String param);
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 19 mar. 2026 - 11:33:54 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/firmarArchivo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response firmarArchivo(@MultipartForm DocumentoDTO documento);
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 19 mar. 2026 - 11:33:51 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/verificarDocumento")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response verificarDocumento(@MultipartForm DocumentoDTO documento);
    
    
    /***
     * 
     *
     * @author Administrador
     * @history 19 mar. 2026 - 12:01:07 Administrador
     * Versión inicial.
     * @param documento
     * @return
     */
    @POST
    @Path("/pdfToCertificados")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response pdfToCertificados(@MultipartForm DocumentoDTO documento);
   
}
