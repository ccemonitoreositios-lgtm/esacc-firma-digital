package ec.gob.cce.global;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Wilfredo Martel
 */
public class ServerConfiguration {

    private static final String RUTA_WINDOWS = "C:\\esacc\\server-properties\\server.properties";
    private static final String RUTA_OTROS = "/etc/wildfly/sacc/server-properties/server.properties";

    private static ServerConfiguration instance = null;

    public static ServerConfiguration getInstance() {
        if (instance == null) {
            instance = new ServerConfiguration();
        }
        return instance;

    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private Properties serverProperties = new Properties();

    public ServerConfiguration() {
        try {

            if (isWindows()) {
                this.serverProperties.load(new FileInputStream(RUTA_WINDOWS));
            } else {
                this.serverProperties.load(new FileInputStream(RUTA_OTROS));
            }
        } catch (final IOException e) {
            throw new RuntimeException("No es posible leer el archivo server.properties");
        }
    }

    /**
     * Retorna la url base del api para descarga de documentos desde hadoop
     *
     * @author segundo.sagnay
     * @history 8 sep. 2020 - 12:18:16 segundo.sagnay
     *          Versión inicial.
     * @return url base
     */
    public String getHadoopApi() {
        return this.serverProperties.getProperty("server.hadoop.api");
    }

    
    public String getTemporalFile() {
        return this.serverProperties.getProperty("server.temporal.ruta.file");
    }
    public String getPropiedad(String key) {
        return this.serverProperties.getProperty(key);
    }

    public Properties getPropiedades() {
        return this.serverProperties;
    }
    
    public String getSHA256Decifrador() {
        return this.serverProperties.getProperty("server.sha256.decifrador");
    }
    
    public String getPrivateKeyDecifrador() {
        return this.serverProperties.getProperty("server.private.key.decifrador");
    }

    /**
     * Se utiliza para los servicios de descarga de documentos.
     *
     * @author Wilfredo Martel
     * @history 28 feb. 2020 - 17:27:52 Wilfredo Martel
     *          Versión inicial.
     * @return
     */
    public String getRutaDominio() {
        final String entorno = this.getPropiedad("server.enviroment");
        String ruta = "";
        switch (entorno) {
            case "produccion":
                ruta = this.getPropiedad("server.domain.produccion");
                break;
            case "prueba":
                ruta = this.getPropiedad("server.domain.prueba");
                break;
            case "desarrollo":
                ruta = this.getPropiedad("server.domain.desarrollo");
                break;
            default:
                ruta = this.getPropiedad("server.domain.desarrollo");
                break;
        }
        return ruta;
    }

}
