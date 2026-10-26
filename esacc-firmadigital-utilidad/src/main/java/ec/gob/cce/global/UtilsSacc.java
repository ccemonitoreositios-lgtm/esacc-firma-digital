package ec.gob.cce.global;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextMarginFinder;

import ec.gob.cce.firmadigital.excepciones.FirmaElectronicaException;
import ec.gob.cce.firmadigital.global.CredencialFirmaDTO;
import ec.gob.cce.firmadigital.global.DocumentoDTO;

public class UtilsSacc {
    private static final Point PUNTO_INICIAL = new Point(67, 730);

    public static DocumentoDTO calcularPosicionEstampa(byte[] documento, Integer numeroFirma) {

        Point puntoInicial = PUNTO_INICIAL;
        final DocumentoDTO document = new DocumentoDTO();
        document.setFile(documento);

        try {

            PdfReader reader = new PdfReader(new java.io.ByteArrayInputStream(documento));
            PdfDocument pdfDoc = new PdfDocument(reader);

            int lastPage = pdfDoc.getNumberOfPages();

            TextMarginFinder finder = new TextMarginFinder();
            PdfCanvasProcessor processor = new PdfCanvasProcessor(finder);
            processor.processPageContent(pdfDoc.getPage(lastPage));

            int posicionInicialY = (int) finder.getTextRectangle().getBottom() - 20;

            if (posicionInicialY > 60) {

                document.setPoint(calcularPosicion(new Point(puntoInicial.x, posicionInicialY), numeroFirma));

            } else {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                PdfDocument newPdf = new PdfDocument(
                        new PdfReader(new java.io.ByteArrayInputStream(documento)),
                        new PdfWriter(baos)
                );

              

                PageSize pageSize = new PageSize(newPdf.getPage(1).getPageSize());
                newPdf.addNewPage(pageSize);

                newPdf.close();
                
                newPdf.close();

                document.setPoint(puntoInicial);
                document.setFile(baos.toByteArray());
            }

            pdfDoc.close();

            return document;

        } catch (IOException e) {

            document.setPoint(puntoInicial);
            return document;

        } catch (NullPointerException e) {

            document.setPoint(calcularPosicion(puntoInicial, numeroFirma));
            return document;
        }
    }

    private static Point calcularPosicion(Point puntoInicial, Integer numeroFirma) {

        final Integer anchoEstampa = 160;
        final Integer altoEstampa = 60;
        final Integer maximoFirmasPorFila = 3;

        final Integer fila = ((numeroFirma - 1) / maximoFirmasPorFila);
        final Integer factorEscala = (maximoFirmasPorFila * fila) + 1;

        final Integer posicionX = puntoInicial.x + ((numeroFirma - factorEscala) * anchoEstampa);
        final Integer posicionY = puntoInicial.y - (fila * altoEstampa);

        return new Point(posicionX, posicionY);
    }

    public static CredencialFirmaDTO obtenerCredencialesFirma(String mensaje, String certificado)
            throws FirmaElectronicaException {

        try {

            final DescifradorUtil cipherUtil = new DescifradorUtil();
            final JsonUtil jsonUtil = new JsonUtil();

            final String jsonDescipher = cipherUtil.descifrarMensaje(mensaje);
            final CredencialFirmaDTO credencialFirma =
                    jsonUtil.jsonDecodedToClass(jsonDescipher, CredencialFirmaDTO.class);

            final Date tokenDate = new Date(credencialFirma.getExpiryTime());
            final Date now = new Date();

            if (now.after(tokenDate)) {
                throw new FirmaElectronicaException("El token de firma ha expirado");
            }

            final byte[] cert = jsonUtil.strBase64ToBytes(certificado);
            credencialFirma.setCertificado(cert);

            return credencialFirma;

        } catch (Exception e) {
            throw new FirmaElectronicaException(e);
        }
    }
}