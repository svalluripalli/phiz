package gov.hhs.onc.phiz.logging.logstash.impl;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import java.util.List;

public class PhizLogstashModule extends Module {
    private String name;
    private List<JsonSerializer<?>> serializers;

    @Override
    public void setupModule(SetupContext setupContext) {
        setupContext.addSerializers(new SimpleSerializers(this.serializers));
    }

    @Override
    public String getModuleName() {
        return this.name;
    }

    public void setModuleName(String name) {
        this.name = name;
    }

    public List<JsonSerializer<?>> getSerializers() {
        return this.serializers;
    }

    public void setSerializers(List<JsonSerializer<?>> serializers) {
        this.serializers = serializers;
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }
}
