package gov.hhs.onc.phiz.test.impl;

import gov.hhs.onc.phiz.beans.factory.EmbeddedPlaceholderResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.CannotReadScriptException;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;

public class PlaceholderResourceDatabasePopulator implements DatabasePopulator {
    @Autowired
    private EmbeddedPlaceholderResolver embeddedPlaceholderResolver;

    private Resource[] scripts;
    private Charset scriptEnc;

    @Override
    public void populate(Connection conn) throws ScriptException {
        new ResourceDatabasePopulator(false, false, this.scriptEnc.name(), Stream
            .of(this.scripts)
            .map(
                script -> {
                    EncodedResource encScript = new EncodedResource(script, this.scriptEnc);

                    try {
                        return new ByteArrayResource(this.embeddedPlaceholderResolver.resolvePlaceholders(
                            IOUtils.toString(encScript.getInputStream(), this.scriptEnc)).getBytes(this.scriptEnc));
                    } catch (IOException e) {
                        throw new CannotReadScriptException(encScript, e);
                    }
                }).toArray(Resource[]::new)).populate(conn);
    }

    public Charset getScriptEncoding() {
        return this.scriptEnc;
    }

    public void setScriptEncoding(Charset scriptEnc) {
        this.scriptEnc = scriptEnc;
    }

    public Resource[] getScripts() {
        return this.scripts;
    }

    public void setScripts(Resource[] scripts) {
        this.scripts = scripts;
    }
}
