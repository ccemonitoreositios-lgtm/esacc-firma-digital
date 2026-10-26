package ec.gob.cce.firmadigital.excepciones;

/**
 * 
 * @author wilfredo.martel
 *
 */
public class UpdateException extends Exception{
    
    private static final long serialVersionUID = 1L;
    public static final String FORMATO_MENSAJE = "Se produjo un error al intentar actualizar datos de un registro en la tabla %s, entidad %s.";

    public UpdateException(Object entity) {
        super(GenericException.format(FORMATO_MENSAJE, entity));
    }

    public UpdateException(Object entity, Throwable ex) {
        super(GenericException.format(FORMATO_MENSAJE, entity), ex);
    }

}
