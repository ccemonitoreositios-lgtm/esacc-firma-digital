package ec.gob.cce.firmadigital.global;



import java.awt.Point;

import org.jboss.resteasy.annotations.jaxrs.FormParam;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentoDTO {

    private Long id;
    private String nombre;
    private String uuid;
    private String repositorio;
    private String carpeta;
    private Integer cantidadFirmas;
    private String fileBase64;
      
    @FormParam("file")
    private byte[] file;
    
    private String usuario;

    @FormParam("contrasenia")
    private String contrasenia;
    private String pin;

    @FormParam("certificado")
    private byte[] certificado;

    private String datosAdicionales;
    
    public Long expiryTime;

    @FormParam("numeroFirma")
    private Integer numeroFirma;
    @FormParam("modulo")
    private String modulo;
    
    private Point point;
    
    public DocumentoDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRepositorio() {
        return repositorio;
    }

    public void setRepositorio(String repositorio) {
        this.repositorio = repositorio;
    }

    public String getCarpeta() {
        return carpeta;
    }

    public void setCarpeta(String carpeta) {
        this.carpeta = carpeta;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
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

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Integer getCantidadFirmas() {
        return cantidadFirmas;
    }

    public void setCantidadFirmas(Integer cantidadFirmas) {
        this.cantidadFirmas = cantidadFirmas;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public Integer getNumeroFirma() {
        return numeroFirma;
    }

    public void setNumeroFirma(Integer numeroFirma) {
        this.numeroFirma = numeroFirma;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }


    
    
    
}
