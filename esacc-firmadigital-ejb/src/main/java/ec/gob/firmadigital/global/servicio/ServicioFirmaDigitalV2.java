package ec.gob.firmadigital.global.servicio;

import java.io.IOException;
import java.util.List;

import ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException;
import ec.gob.cce.firmadigital.global.CredencialFirmaDTO;
import ec.gob.cce.firmadigital.global.DatosCertificadoDigitalDTO;
import ec.gob.firmadigital.libreria.certificate.to.Certificado;
import ec.gob.firmadigital.libreria.exceptions.SignatureVerificationException;
import jakarta.ejb.Local;

@Local
public interface ServicioFirmaDigitalV2 {

    
    /**
     * Permite obtener las credenciales de firma.
     *
     * @author Administrador
     * @history 17 mar. 2026 - 09:11:30 Administrador
     * Versión inicial.
     * @param mensaje
     * @param certificado
     * @return
     * @throws FirmaElectronicaException
     */
    public CredencialFirmaDTO obtenerCredencialesFirma(String mensaje, byte[] certificado) throws FirmaElectronicaException;
    
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 17 mar. 2026 - 11:15:34 Administrador
     * Versión inicial.
     * @param credenciales
     * @return
     * @throws FirmaElectronicaException
     */
    public boolean validarCredenciales(CredencialFirmaDTO credenciales) throws FirmaElectronicaException;
    
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 17 mar. 2026 - 13:37:23 Administrador
     * Versión inicial.
     * @param credenciales
     * @param archivo
     * @param numeroFirma
     * @param modulo
     * @return
     * @throws FirmaElectronicaException
     */
    public byte[] firmarArchivo(CredencialFirmaDTO credenciales, byte[] archivo, Integer numeroFirma, String modulo) throws FirmaElectronicaException;
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 18 mar. 2026 - 12:23:15 Administrador
     * Versión inicial.
     * @param credenciales
     * @param validarUsuario
     * @return
     * @throws FirmaElectronicaException
     */
    public DatosCertificadoDigitalDTO obtenerDatosCertificado(CredencialFirmaDTO credenciales, Boolean validarUsuario) throws FirmaElectronicaException;
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 19 mar. 2026 - 11:22:53 Administrador
     * Versión inicial.
     * @param file
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public Boolean verificarDocumento(byte[] file) throws IOException, SignatureVerificationException, Exception;
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 19 mar. 2026 - 11:58:49 Administrador
     * Versión inicial.
     * @param file
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public List<Certificado> pdfToCertificados(byte[] file) throws IOException, SignatureVerificationException, Exception;
   
}
