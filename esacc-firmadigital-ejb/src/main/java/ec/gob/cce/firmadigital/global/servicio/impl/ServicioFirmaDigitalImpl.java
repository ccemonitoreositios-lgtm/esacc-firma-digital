package ec.gob.cce.firmadigital.global.servicio.impl;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tika.mime.MimeTypeException;

import ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException;
import ec.gob.cce.firmadigital.global.CredencialFirmaDTO;
import ec.gob.cce.firmadigital.global.DocumentoDTO;
import ec.gob.cce.firmadigital.global.ResponseDTO;
import ec.gob.cce.firmadigital.global.RespuestaArchivoDTO;
import ec.gob.cce.global.DescifradorUtil;
import ec.gob.cce.global.JsonUtil;
import ec.gob.cce.global.ServerConfiguration;
import ec.gob.cce.global.UtilsSacc;
import ec.gob.firmadigital.global.servicio.ServicioFirmaDigital;
import ec.gob.firmadigital.libreria.certificate.CertUtils;
import ec.gob.firmadigital.libreria.certificate.to.Documento;
import ec.gob.firmadigital.libreria.exceptions.HoraServidorException;
import ec.gob.firmadigital.libreria.exceptions.RubricaException;
import ec.gob.firmadigital.libreria.exceptions.SignatureVerificationException;
import ec.gob.firmadigital.libreria.keystore.FileKeyStoreProvider;
import ec.gob.firmadigital.libreria.keystore.KeyStoreProvider;
import ec.gob.firmadigital.libreria.sign.DigestAlgorithm;
import ec.gob.firmadigital.libreria.sign.PrivateKeySigner;
import ec.gob.firmadigital.libreria.sign.pdf.BasePdfSigner;
import ec.gob.firmadigital.libreria.sign.pdf.PadesBasicSigner;
import ec.gob.firmadigital.libreria.sign.pdf.RectanguloUtil;
import ec.gob.firmadigital.libreria.utils.Utils;
import ec.gob.firmadigital.libreria.utils.X509CertificateUtils;
import eu.europa.esig.dss.model.InMemoryDocument;
import jakarta.ejb.Stateless;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;



@Stateless
public class ServicioFirmaDigitalImpl implements ServicioFirmaDigital {

    private final Logger log = Logger.getLogger(this.getClass());
    private final JsonUtil jsonUtil = new JsonUtil();

    @Override
    public ResponseDTO<Boolean> verificarFirmaElectronicaPorUuid(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
        byte[] pdfBytes = null;
      
        if (isNullOrEmpty(documento.getUuid()) || isNullOrEmpty(documento.getCarpeta())) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo es nulo o está vacío");
            return respuesta;
        }

        try {
            
            String url = generarUrlDocumento(documento.getCarpeta(), documento.getUuid() );  
            deshabilitarSSL();
            pdfBytes = this.descargarPdfComoByteArray(url);
            
            if (null == pdfBytes || isNullOrEmpty(pdfBytes)) {
                respuesta.setDato(Boolean.FALSE);
                respuesta.setMensaje("El archivo descargado es nulo o está vacío");
                return respuesta;
            }

        } catch (Exception e) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo no se encuetra en el repositorio hadoop.");
            return respuesta;
        }

       
        Documento d = Utils.verificarDocumentoInterno(pdfBytes);
       

        if (!d.getSignValidate() && d.getDocValidate()) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo contiene firmas invalidas.");
            return respuesta;
        }
        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo no es valido y contiene firmas invalidas.");
            return respuesta;
        }

        if (d.getSignValidate() && d.getDocValidate()) {
            log.info("Documento válido...");
            respuesta.setDato(true);
            respuesta.setMensaje(null);
            return respuesta;
        }

        // Caso por defecto: alguna validación falló y hay un error
        respuesta.setDato(false);
        respuesta.setMensaje(d.getError());
        return respuesta;
    }
    
    @Override
    public ResponseDTO<Boolean> verificarFirmaElectronicaSacc(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
        if (isNullOrEmpty(documento.getFile())) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo es nulo o está vacío");
            return respuesta;
        }

        Documento d = Utils.verificarDocumentoInterno(documento.getFile());
        if (d.getSignValidate() && d.getDocValidate()) {
            log.info("Documento válido...");
            respuesta.setDato(true);
            respuesta.setMensaje(null);
            return respuesta;
        }

        if (!d.getSignValidate() && d.getDocValidate()) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo contiene firmas invalidas.");
            return respuesta;
        }
        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo no es valido y contiene firmas invalidas.");
            return respuesta;
        }

        // Caso por defecto: alguna validación falló y hay un error
        respuesta.setDato(false);
        respuesta.setMensaje(d.getError());
        return respuesta;
    }
    
    @Override
    public ResponseDTO<Boolean> verificarFirmaElectronicaSaccBase64(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();
        if (isNullOrEmpty(documento.getFileBase64())) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo es nulo o está vacío");
            return respuesta;
        }
        byte[] pdfBytes = null;
        try {
           pdfBytes = Base64.getDecoder().decode(documento.getFileBase64());
        } catch (Exception e) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo es ilegible. ");
            return respuesta;
        }
       

        Documento d = Utils.verificarDocumentoInterno(pdfBytes);
        if (d.getSignValidate() && d.getDocValidate()) {
            log.info("Documento válido...");
            respuesta.setDato(true);
            respuesta.setMensaje(null);
            return respuesta;
        }

        if (!d.getSignValidate() && d.getDocValidate()) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo contiene firmas invalidas.");
            return respuesta;
        }
        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo no es valido y contiene firmas invalidas.");
            return respuesta;
        }

        // Caso por defecto: alguna validación falló y hay un error
        respuesta.setDato(false);
        respuesta.setMensaje(d.getError());
        return respuesta;
    }

    @Override
    public ResponseDTO<Boolean> verificarFirmaElectronicaProcesoFirmaMasiva(DocumentoDTO documento) throws IOException, SignatureVerificationException, Exception {
        ResponseDTO<Boolean> respuesta = new ResponseDTO<>();

        if (isNullOrEmpty(documento.getFile())) {
            respuesta.setDato(Boolean.FALSE);
            respuesta.setMensaje("El archivo es nulo o está vacío");
            return respuesta;
        }
        Documento d = Utils.verificarDocumentoInterno(documento.getFile());
       

        if (!d.getSignValidate() && d.getDocValidate()) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo contiene firmas invalidas.");
            return respuesta;
        }
        
        if (d.getSignValidate() && !d.getDocValidate()) {
            respuesta.setDato(false);
            respuesta.setMensaje("Todo archivo no es válido.");
            return respuesta;
        }

        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {
            respuesta.setDato(false);
            respuesta.setMensaje("El archivo no es valido y contiene firmas invalidas.");
            return respuesta;
        }
        log.info("Documento valido...");
        respuesta.setMensaje(null);
        respuesta.setDato(Boolean.TRUE);
        return respuesta;

    }

    public ResponseDTO<RespuestaArchivoDTO> firmarDocumentoProcesoFirmaMasiva(DocumentoDTO documento)
            throws UnsupportedEncodingException, FirmaElectronicaException, KeyStoreException, MimeTypeException, IOException, RubricaException,
            UnrecoverableKeyException, NoSuchAlgorithmException, HoraServidorException, SignatureVerificationException, Exception

    {
        byte[] result = null;
        ResponseDTO<RespuestaArchivoDTO> respuesta = new ResponseDTO<>();

        if (isNullOrEmpty(documento.getFile()) || isNullOrEmpty(documento.getCertificado()) || isNullOrEmpty(documento.getContrasenia())) {
            respuesta.setDato(null);
            respuesta.setMensaje("Los archivos es nulo o está vacío");
            return respuesta;
        }

        CredencialFirmaDTO paswordCredencial = obtenerCredencialesFirma(documento.getContrasenia(), documento.getCertificado());
        KeyStore keyStore = obtenerKeyStorePorCredencial(paswordCredencial);

     
        Documento documentoVerificado = Utils.verificarDocumentoInterno(documento.getFile()); 
 
        Integer cantidadFirmas = documentoVerificado.getCertificados().size();
        
        DocumentoDTO documentoNuevo = UtilsSacc.calcularPosicionEstampa(documento.getFile(), cantidadFirmas + 1);
      
       
        String alias = CertUtils.seleccionarAlias(keyStore, null);
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, paswordCredencial.getContrasenia().toCharArray());

        X509CertificateUtils x509CertificateUtils = new X509CertificateUtils();
       // if (x509CertificateUtils.validarX509Certificate((X509Certificate) keyStore.getCertificate(alias), null)) {//validación de firmaEC
        Certificate[] certChain = keyStore.getCertificateChain(alias);

        Properties properties = parametros(0, documentoNuevo.getPoint());

        
        try {   
          
            PrivateKeySigner privateKeySigner = new PrivateKeySigner(key, DigestAlgorithm.forName("SHA512"));
         
            PadesBasicSigner pdfSigner = new  PadesBasicSigner(privateKeySigner);
            InMemoryDocument inMemoryDocument = new InMemoryDocument(documentoNuevo.getFile());
            InputStream is = inMemoryDocument.openStream();
            result =  pdfSigner.sign(is, key, certChain, properties);
         

        } finally {
            log.info("Archivo firmado correntamente..");
        }        
        RespuestaArchivoDTO rsp = new RespuestaArchivoDTO();
        rsp.setFile(result);     
        respuesta.setDato(rsp);
        respuesta.setMensaje(null);
        return respuesta;

    }

 
  

    private static KeyStore obtenerKeyStoreByte(DocumentoDTO credenciales) throws KeyStoreException {
        InputStream inputStreamFirma = new ByteArrayInputStream(credenciales.getCertificado());
        final KeyStoreProvider ksp = new FileKeyStoreProvider(inputStreamFirma);
        final KeyStore ks = ksp.getKeystore(credenciales.getContrasenia().toCharArray());
        return ks;

    }

  
    private static KeyStore obtenerKeyStorePorCredencial(CredencialFirmaDTO credenciales) throws KeyStoreException {
        InputStream inputStreamFirma = new ByteArrayInputStream(credenciales.getCertificado());
        final KeyStoreProvider ksp = new FileKeyStoreProvider(inputStreamFirma);
        final KeyStore ks = ksp.getKeystore(credenciales.getContrasenia().toCharArray());
        return ks;

    }

    private static Properties parametros(Integer ultimaPagina, Point punto) throws IOException, HoraServidorException {

        Properties params = new Properties();

        params.setProperty(BasePdfSigner.LAST_PAGE, String.valueOf(ultimaPagina));
        params.setProperty(BasePdfSigner.INFO_QR, "Sistema Automatizado de la Corte Constitucional");
        params.setProperty(BasePdfSigner.TYPE_SIG, "QR");
        params.setProperty(RectanguloUtil.POSITION_ON_PAGE_LOWER_LEFT_X, String.valueOf(Math.round(punto.getX())));
        params.setProperty(RectanguloUtil.POSITION_ON_PAGE_LOWER_LEFT_Y, String.valueOf(Math.round(punto.getY())));
    //    params.setProperty(BasePdfSigner.SIGN_TIME, TiempoUtils.getFechaHoraServidor(null, PropertiesUtils.versionBase64()));
        params.setProperty(BasePdfSigner.SIGN_TIME, ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return params;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isNullOrEmpty(byte[] file) {
        return file == null || file.length == 0;
    }

   
    




    private CredencialFirmaDTO obtenerCredencialesFirma(String mensaje, byte[] certificado) throws FirmaElectronicaException {
        try {
            final DescifradorUtil cipherUtil = new DescifradorUtil();
            final JsonUtil jsonUtil = new JsonUtil();
            final String jsonDescipher = cipherUtil.descifrarMensaje(mensaje);
            final CredencialFirmaDTO credencialFirma = jsonUtil.jsonDecodedToClass(jsonDescipher, CredencialFirmaDTO.class);
            final Date tokenDate = new Date(credencialFirma.getExpiryTime());
            final Date now = new Date();
            if (now.after(tokenDate)) {
                throw new FirmaElectronicaException("El token de firma ha expirado");
            }

            credencialFirma.setCertificado(certificado);
            return credencialFirma;
        } catch (final Exception e) {
            throw new FirmaElectronicaException(e);
        }
    }
    
    private static String generarUrlDocumento(String carpeta, String uuid) {
        final String urlHadoopApi = ServerConfiguration.getInstance().getHadoopApi();
        String data = String.format("{carpeta:'%s', uuid:'%s'}", carpeta, uuid); // ← Aquí se añaden las llaves
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes());
        return urlHadoopApi + encoded;
    }

    private static byte[] descargarPdfComoByteArray(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Verificar si la respuesta es exitosa
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            }
        } else {
            throw new IOException("Error al descargar el PDF. Código HTTP: " + connection.getResponseCode());
        }
    }
    
    public static void deshabilitarSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // No valida
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // No valida
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

}
