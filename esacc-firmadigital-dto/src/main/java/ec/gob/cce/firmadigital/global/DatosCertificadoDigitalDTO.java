/*******************************************************************************
 * Copyright (c) 2019 - 2022 Corte Constitucional del Ecuador. All rights reserved.
 * Contributors:
 * Dirección Nacional de Tecnología de la Información
 ******************************************************************************/
package ec.gob.cce.firmadigital.global;

import java.security.KeyStore;

public class DatosCertificadoDigitalDTO {
    private KeyStore keyStore;
    private String alias;

    public DatosCertificadoDigitalDTO() {
        super();
    }

    public DatosCertificadoDigitalDTO(KeyStore ks, String alias) {
        super();
        this.setKeyStore(ks);
        this.setAlias(alias);
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
