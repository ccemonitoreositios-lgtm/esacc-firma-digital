package ec.gob.cce.firmadigital.global.servicio.impl;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Vector;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextMarginFinder;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.InvalidPdfException;

import ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException;
import ec.gob.cce.firmadigital.global.CredencialFirmaDTO;
import  ec.gob.cce.firmadigital.global.DatosCertificadoDigitalDTO;
import ec.gob.cce.firmadigital.global.DocumentoDTO;
import ec.gob.cce.global.DescifradorUtil;
import ec.gob.cce.global.JsonUtil;
import ec.gob.cce.global.UtilsSacc;
import ec.gob.firmadigital.global.servicio.ServicioFirmaDigitalV2;
import ec.gob.firmadigital.libreria.certificate.CertEcUtils;
import ec.gob.firmadigital.libreria.certificate.CertUtils;
import ec.gob.firmadigital.libreria.certificate.to.Certificado;
import ec.gob.firmadigital.libreria.certificate.to.DatosUsuario;
import ec.gob.firmadigital.libreria.certificate.to.Documento;
import ec.gob.firmadigital.libreria.exceptions.EntidadCertificadoraNoValidaException;
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

@Stateless
public class ServicioFirmaDigitalV2Impl implements ServicioFirmaDigitalV2 {

    private final Logger log = Logger.getLogger(this.getClass());
    private final JsonUtil jsonUtil = new JsonUtil();
    private final String INFO_QR = "Sistema Automatizado de la Corte Constitucional";
    private final String TYPE_SIG = "QR";
  
    
    @Override
    public CredencialFirmaDTO obtenerCredencialesFirma(String mensaje, byte[] certificado) throws FirmaElectronicaException {
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
    
    
    @Override
    public boolean validarCredenciales(CredencialFirmaDTO credenciales) throws FirmaElectronicaException {
        final DatosCertificadoDigitalDTO datos = this.obtenerDatosCertificado(credenciales, false);
        return (datos != null);
    }
    @Override
    public byte[] firmarArchivo(CredencialFirmaDTO credenciales, byte[] archivo, Integer numeroFirma, String modulo) throws FirmaElectronicaException {
        final DatosCertificadoDigitalDTO datosCertificado = this.obtenerDatosCertificado(credenciales, false);
        byte[] firmado = null;
        DocumentoDTO documentoDTO = new DocumentoDTO();
        if (null != datosCertificado) {
            if (modulo.equalsIgnoreCase("DOCUMENTAL")) {
                documentoDTO = this.calcularPosicionEstampaDocumental(archivo, numeroFirma);
            } else if(modulo.equalsIgnoreCase("FIRMAR_LOTE")) {
                documentoDTO = this.calcularPosicionEstampaPiePagina(archivo, numeroFirma);
            } else {
                documentoDTO = this.calcularPosicionEstampa(archivo, numeroFirma);
            }
            firmado = this.firmar(datosCertificado.getKeyStore(), datosCertificado.getAlias(), documentoDTO.getFile(), credenciales.getPassword(), documentoDTO.getPoint(),
                    0, "", true);
        }
        return firmado;
    }
    
    
    
    
    @Override
    public DatosCertificadoDigitalDTO obtenerDatosCertificado(CredencialFirmaDTO credenciales, Boolean validarUsuario) throws FirmaElectronicaException {
        try {

            InputStream inputStreamFirma = new ByteArrayInputStream(credenciales.getCertificado());
            final KeyStoreProvider ksp = new FileKeyStoreProvider(inputStreamFirma);
            final KeyStore ks = ksp.getKeystore(credenciales.getContrasenia().toCharArray());
            
            if (null != ks) {
                final String alias = CertUtils.seleccionarAlias(ks, null);
                final X509Certificate x509Certificado = CertUtils.getCert(ks, alias);
                final X509CertificateUtils x509CertificateUtils = new X509CertificateUtils();
                if ((null != x509Certificado) && (null != alias) && x509CertificateUtils.validarX509Certificate(x509Certificado) ) { 
                    if (Boolean.TRUE.equals(validarUsuario)) {
                        this.validarDatosUsuario(credenciales, x509Certificado);
                    }
                    return new DatosCertificadoDigitalDTO(ks, alias);
                }
            }
            return null;
        } catch (final KeyStoreException e) {
            if (e.getMessage().equals("java.io.IOException: keystore password was incorrect")) {
                throw new FirmaElectronicaException("La contraseña de firma electrónica es incorrecta, por favor verifique e intente nuevamente");
            } else {
                throw new FirmaElectronicaException("Revise el certificado, puede que este vacío o el archivo este corrupto");
            }
        } catch (Exception e) {
            throw new FirmaElectronicaException(e);
        }
    }
    
    
    
    @Override
    public Boolean verificarDocumento(byte[] file) throws IOException, SignatureVerificationException, Exception {
      
        if (isNullOrEmpty(file)) {           
            return false;
        }

        Documento d = Utils.verificarDocumentoInterno(file);
        if (d.getSignValidate() && d.getDocValidate()) {
            log.info("Documento válido...");           
            return true;
        }

        if (!d.getSignValidate() && d.getDocValidate()) {   
            log.info("Documento no válido 1...");   
            return false;
        }
        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {  
            log.info("Documento no válido 2...");         
            return false;
        }
        log.info("Documento no válido 3...");   
        return false;
    }
    
    @Override
    public List<Certificado> pdfToCertificados(byte[] file) throws IOException, SignatureVerificationException, Exception {
        
        if (isNullOrEmpty(file)) {           
            return null;
        }

        Documento d = Utils.verificarDocumentoInterno(file);
        if (d.getSignValidate() && d.getDocValidate()) {
            log.info("Documento válido...");           
            return d.getCertificados();
        }

        if (!d.getSignValidate() && d.getDocValidate()) {   
            log.info("Documento no válido 1...");   
            return null;
        }
        if (!d.getSignValidate() && !d.getDocValidate() && isNullOrEmpty(d.getError())) {  
            log.info("Documento no válido 2...");         
            return null;
        }
        log.info("Documento no válido 3...");   
        return null;
    }
    
    
    
    private byte[] firmar(
            KeyStore keyStore,
            String alias,
            byte[] documento,
            char[] password,
            Point posicion, 
            int pagina, 
            String razonFirma, 
            Boolean firmaVisible)
            throws FirmaElectronicaException {
        try {
            byte[] firmado = null;
          //  final Signer signer = Utils.documentSigner(documento);

            // Propiedades para personalizar la firma
            final Properties params = new Properties();
            params.setProperty(BasePdfSigner.SIGNING_LOCATION, "");
            params.setProperty(BasePdfSigner.SIGNING_REASON, razonFirma);
            params.setProperty(BasePdfSigner.SIGN_TIME, ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            if (Boolean.TRUE.equals(firmaVisible)) {
                // Posicion firma
                params.setProperty(BasePdfSigner.LAST_PAGE, String.valueOf(pagina));
                params.setProperty(BasePdfSigner.TYPE_SIG, TYPE_SIG);
                params.setProperty(BasePdfSigner.INFO_QR, INFO_QR);
                params.setProperty(RectanguloUtil.POSITION_ON_PAGE_LOWER_LEFT_X, String.valueOf(posicion.x));
                params.setProperty(RectanguloUtil.POSITION_ON_PAGE_LOWER_LEFT_Y, String.valueOf(posicion.y));
            }
            // Obtiene la clave privada
            final PrivateKey key = (PrivateKey) keyStore.getKey(alias, password);
            // Obtener el certificate chain:
            final Certificate[] certChain = keyStore.getCertificateChain(alias);
            if (key != null) {
                PrivateKeySigner privateKeySigner = new PrivateKeySigner(key, DigestAlgorithm.forName("SHA512"));                
                PadesBasicSigner pdfSigner = new  PadesBasicSigner(privateKeySigner);
                InMemoryDocument inMemoryDocument = new InMemoryDocument(documento);
                InputStream is = inMemoryDocument.openStream();
                firmado =  pdfSigner.sign(is, key, certChain, params);
            }
            return firmado;
        } catch (final java.security.UnrecoverableKeyException uke) {
            throw new FirmaElectronicaException("Problemas con el certificado digital, favor contacte con su Entidad Certificadora");
        } catch (final InvalidPdfException ipe) {
            throw new FirmaElectronicaException("Problemas al abrir el documento");
        } catch (final BadPasswordException bpe) {
            throw new FirmaElectronicaException("Documento protegido con contraseña");
        } catch ( ExceptionConverter | KeyStoreException | NoSuchAlgorithmException |  IOException e) {
            throw new FirmaElectronicaException(e);
        }
    }
    
    

    
    
    private DocumentoDTO calcularPosicionEstampaDocumental(byte[] documento, Integer numeroFirma) {
        final DocumentoDTO document = new DocumentoDTO();
        document.setFile(documento);
        try {

            PdfReader reader = new PdfReader(new java.io.ByteArrayInputStream(documento));
            final Integer[] fontPosition = getFontPosition(reader, "sacc_cce_fe_2021.", null);
            document.setPoint(new Point(fontPosition[0], fontPosition[1]));
            return document;
        } catch (final IOException e) {
            document.setPoint(this.calcularPosicion(new Point(67, 730), numeroFirma));
            return document;
        } catch (final NullPointerException e) {
            document.setPoint(this.calcularPosicion(new Point(67, 730), numeroFirma));
            return document;
        }
    }
    
    
    public static Integer[] getFontPosition(PdfReader pdfReader, final String keyWord, Integer pageNum) throws IOException {

        final Integer[] result = new Integer[2];

        PdfDocument pdfDoc = new PdfDocument(pdfReader);
        pageNum = pdfDoc.getNumberOfPages();

        PdfPage page = pdfDoc.getPage(pageNum);

        IEventListener listener = new IEventListener() {

            @Override
            public void eventOccurred(IEventData data, EventType type) {

                if (type == EventType.RENDER_TEXT) {

                    TextRenderInfo textRenderInfo = (TextRenderInfo) data;
                    String text = textRenderInfo.getText();

                    if (text != null && text.contains(keyWord)) {

                        Vector start = textRenderInfo.getBaseline().getStartPoint();

                        float x = start.get(0);
                        float y = start.get(1);

                        result[0] = (int) x;
                        result[1] = (int) y;
                    }
                }
            }

            @Override
            public Set<EventType> getSupportedEvents() {
                return Collections.singleton(EventType.RENDER_TEXT);
            }
        };

        PdfCanvasProcessor processor = new PdfCanvasProcessor(listener);
        processor.processPageContent(page);

        pdfDoc.close();

        return result;
    }
    
    
    private static Point calcularPosicion(Point puntoinicial, Integer numeroFirma) {
        final Integer anchoEstampa = 160;
        final Integer altoEstampa = 60;
        final Integer maximoFirmasPorFila = 3;
        final Integer fila = ((numeroFirma - 1) / maximoFirmasPorFila);
        final Integer factorEscala = (maximoFirmasPorFila * fila) + 1;
        final Integer posicionX = puntoinicial.x + ((numeroFirma - factorEscala) * anchoEstampa);
        final Integer posicionY = puntoinicial.y - (fila * altoEstampa);
        return new Point(posicionX, posicionY);
    }
    
    
    public DocumentoDTO calcularPosicionEstampa(byte[] documento, Integer numeroFirma) {
       return  UtilsSacc.calcularPosicionEstampa(documento, numeroFirma);
    }
    
    
  
    
    
  

    private void validarDatosUsuario(CredencialFirmaDTO credenciales, X509Certificate x509Certificado) throws FirmaElectronicaException, EntidadCertificadoraNoValidaException {
        final DatosUsuario datosUsuario = CertEcUtils.getDatosUsuarios(x509Certificado);
        if (null == datosUsuario) {
            throw new FirmaElectronicaException("No se pudo extraer los datos del certificado.");
        }
        if (!datosUsuario.getCedula().equals(credenciales.getUsuario())) {
            throw new FirmaElectronicaException("Los datos de firma electrónica no coinciden con los datos del usuario autenticado.");
        }
    }
    
    
    private DocumentoDTO calcularPosicionEstampaPiePagina(byte[] documento, Integer numeroFirma) {

        final Point puntoInicial = new Point(67, 730);
        final DocumentoDTO document = new DocumentoDTO();
        document.setFile(documento);

        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(documento));
             PdfDocument pdfDoc = new PdfDocument(reader)) {

            float posicionY = obtenerPosicionTexto(pdfDoc, puntoInicial);

            if (posicionY > 60) {
                return construirDocumento(document, new Point(puntoInicial.x, (int) posicionY), numeroFirma);
            }

            return procesarFormulario(pdfDoc, document, puntoInicial, numeroFirma);

        } catch (IOException e) {
            document.setPoint(puntoInicial);
            return document;

        } catch (NullPointerException e) {
            document.setPoint(this.calcularPosicion(puntoInicial, numeroFirma));
            return document;
        }
    }
    
    private float obtenerPosicionTexto(PdfDocument pdfDoc, Point puntoInicial) throws IOException {

        PdfDocumentContentParser parser = new PdfDocumentContentParser(pdfDoc);
        TextMarginFinder finder = parser.processContent(pdfDoc.getNumberOfPages(), new TextMarginFinder());

        return finder.getTextRectangle() != null
                ? finder.getTextRectangle().getY() - 20
                : puntoInicial.y;
    }
    
    private DocumentoDTO procesarFormulario(PdfDocument pdfDoc, DocumentoDTO document, Point puntoInicial, Integer numeroFirma) {

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);

        if (numeroFirma >= 2) {
            return manejarMultiplesFirmas(form, document, numeroFirma);
        } else {
            return manejarPrimeraFirma(pdfDoc, form, document, puntoInicial);
        }
    }
    
    private DocumentoDTO manejarMultiplesFirmas(PdfAcroForm form, DocumentoDTO document, Integer numeroFirma) {

        final float TOLERANCIA = 2.0f;
        final Point puntoInicio = new Point(85, 120);

        boolean existeFirma = existeFirmaEnPosicion(form, 85, 120, TOLERANCIA);

        if (existeFirma) {
            document.setPoint(this.calcularPosicion(puntoInicio, numeroFirma));
        } else {
            document.setPoint(puntoInicio);
        }

        return document;
    }
    
    private boolean existeFirmaEnPosicion(PdfAcroForm form, float x, float y, float tolerancia) {

        if (form == null || form.getAllFormFields() == null) {
            return false;
        }

        for (PdfFormField field : form.getAllFormFields().values()) {

            if (field.getWidgets() != null && !field.getWidgets().isEmpty()) {

                Rectangle rect = field.getWidgets().get(0).getRectangle().toRectangle();

                if (Math.abs(rect.getLeft() - x) < tolerancia &&
                    Math.abs(rect.getTop() - y) < tolerancia) {
                    return true;
                }
            }
        }

        return false;
    }
    
    private DocumentoDTO manejarPrimeraFirma(PdfDocument pdfDoc, PdfAcroForm form, DocumentoDTO document, Point puntoInicial) {

        if (form == null || form.getAllFormFields() == null || form.getAllFormFields().isEmpty()) {
            return agregarPagina(document, pdfDoc, puntoInicial);
        } else {
            document.setPoint(new Point(85, 120));
            return document;
        }
    }
    private DocumentoDTO agregarPagina(DocumentoDTO document, PdfDocument pdfDoc, Point puntoInicial) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfDocument newPdf = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(document.getFile())),
                new PdfWriter(baos))) {

            newPdf.addNewPage(newPdf.getNumberOfPages() + 1);
        } catch (IOException e) {
            // puedes loguear aquí
        }

        document.setPoint(puntoInicial);
        document.setFile(baos.toByteArray());

        return document;
    }
    
    private DocumentoDTO construirDocumento(DocumentoDTO document, Point punto, Integer numeroFirma) {

        document.setPoint(this.calcularPosicion(punto, numeroFirma));
        return document;
    }
    
    private boolean isNullOrEmpty(byte[] file) {
        return file == null || file.length == 0;
    }
    
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


}
