package ec.gob.cce.firmadigital.excepciones;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.TreeMap;
import jakarta.ejb.ApplicationException;
import jakarta.persistence.Table;
/**
 * 
 * @author wilfredo.martel
 *
 */
@ApplicationException(rollback = true)
public class GenericException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static String format(String formato, Object entidad) {
        final Annotation[] anotaciones = entidad.getClass().getDeclaredAnnotations();
        String nombreTabla = entidad.getClass().getSimpleName().toUpperCase();
        for (final Annotation an : anotaciones) {
            if (an.getClass().getName().equals(Table.class.getName())) {
                final Table tb = (Table) an;
                nombreTabla = tb.name();
            }
        }
        return String.format(formato, new Object[] { nombreTabla, entidad.getClass().getCanonicalName() });
    }

    protected Map<String, Object> info = new TreeMap<>();

    public GenericException(String msg) {
        super(msg);
        this.info = new TreeMap<>();
    }

    public GenericException(String msg, Throwable ex) {
        super(msg, ex);
        this.info = new TreeMap<>();
    }

    public GenericException(String msg, Throwable ex, Map<String, Object> aditionalInfo) {
        super(msg, ex);
        this.info = aditionalInfo;
    }

    public GenericException(Throwable ex) {
        super(ex);
        this.info = new TreeMap<>();
    }

    public GenericException addInfo(String key, Object value) {
        this.info.put(key, value);
        return this;
    }

    public Map<String, Object> getInfo() {
        return this.info;
    }

}
