package ec.gob.firmadigital.global.servicio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.tika.mime.MimeTypeException;

import ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException;
import ec.gob.cce.firmadigital.global.DocumentoDTO;
import ec.gob.cce.firmadigital.global.ResponseDTO;
import ec.gob.cce.firmadigital.global.RespuestaArchivoDTO;
import ec.gob.firmadigital.libreria.exceptions.HoraServidorException;
import ec.gob.firmadigital.libreria.exceptions.RubricaException;
import ec.gob.firmadigital.libreria.exceptions.SignatureVerificationException;
import jakarta.ejb.Local;

@Local
public interface ServicioFirmaDigital {

    /**
     * @author Administrador
     * @history 2 abr. 2025 - 11:00:55 Administrador
     *          Versión inicial.
     * @param documento
     * @param respuesta
     * @return
     */
    public ResponseDTO<Boolean> verificarFirmaElectronicaPorUuid(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception;

   
    
    
   
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 4 abr. 2025 - 10:38:00 Administrador
     * Versión inicial.
     * @param documento
     * @return
     * @throws UnsupportedEncodingException
     * @throws FirmaElectronicaException
     * @throws KeyStoreException
     * @throws MimeTypeException
     * @throws IOException
     * @throws RubricaException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws HoraServidorException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public ResponseDTO<RespuestaArchivoDTO> firmarDocumentoProcesoFirmaMasiva(DocumentoDTO documento)
            throws UnsupportedEncodingException, FirmaElectronicaException, KeyStoreException, MimeTypeException, IOException, RubricaException,
            UnrecoverableKeyException, NoSuchAlgorithmException, HoraServidorException, SignatureVerificationException, Exception;
    
    /**
     * 
     *
     * @author Administrador
     * @history 7 abr. 2025 - 14:31:32 Administrador
     * Versión inicial.
     * @param documento
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public ResponseDTO<Boolean> verificarFirmaElectronicaSacc(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception;
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 8 abr. 2025 - 16:43:42 Administrador
     * Versión inicial.
     * @param documento
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public ResponseDTO<Boolean> verificarFirmaElectronicaProcesoFirmaMasiva(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception;
    
    
    /**
     * 
     *
     * @author Administrador
     * @history 8 ene. 2026 - 15:13:26 Administrador
     * Versión inicial.
     * @param documento
     * @return
     * @throws IOException
     * @throws SignatureVerificationException
     * @throws Exception
     */
    public ResponseDTO<Boolean> verificarFirmaElectronicaSaccBase64(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception;

}
