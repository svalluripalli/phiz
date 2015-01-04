package gov.hhs.onc.phiz.crypto.impl;

import gov.hhs.onc.phiz.crypto.PhizKeyStoreEntry;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeneratedKeyStoreFactoryBean extends PhizKeyStoreFactoryBean {
    private Map<String, PhizKeyStoreEntry<? extends Entry>> entryMap = new LinkedHashMap<>();

    @Override
    public KeyStore getObject() throws Exception {
        KeyStore keyStore = this.getObjectInternal(null, null);

        PhizKeyStoreEntry<?> entry;

        for (String entryAlias : entryMap.keySet()) {
            keyStore.setEntry(entryAlias, (entry = entryMap.get(entryAlias)).toEntry(), entry.getProtection());
        }
        
        try (OutputStream outStream = resource.getOutputStream()) {
            keyStore.store(outStream, ((pass != null) ? pass.toCharArray() : null));

            outStream.flush();
        }

        return super.getObject();
    }

    public Map<String, PhizKeyStoreEntry<? extends Entry>> getEntryMap() {
        return this.entryMap;
    }

    public void setEntryMap(Map<String, PhizKeyStoreEntry<? extends Entry>> entryMap) {
        this.entryMap.clear();
        this.entryMap.putAll(entryMap);
    }
}
