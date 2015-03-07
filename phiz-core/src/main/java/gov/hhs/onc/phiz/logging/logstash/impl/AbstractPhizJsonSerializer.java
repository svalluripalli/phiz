package gov.hhs.onc.phiz.logging.logstash.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public abstract class AbstractPhizJsonSerializer<T> extends StdSerializer<T> {
    private final static long serialVersionUID = 0L;

    protected AbstractPhizJsonSerializer(Class<T> objClass) {
        super(objClass);
    }

    @Override
    public void serialize(T obj, JsonGenerator jsonGen, SerializerProvider serializerProv) throws IOException {
        jsonGen.writeStartObject();

        try {
            this.serializeFields(obj, jsonGen, serializerProv);
        } catch (Exception e) {
            throw new IOException(String.format("Unable to serialize object (class=%s).", this._handledType.getName()), e);
        }

        jsonGen.writeEndObject();
    }

    protected abstract void serializeFields(T obj, JsonGenerator jsonGen, SerializerProvider serializerProv) throws Exception;
}
