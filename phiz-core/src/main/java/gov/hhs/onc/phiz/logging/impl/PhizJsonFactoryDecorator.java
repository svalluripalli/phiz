package gov.hhs.onc.phiz.logging.impl;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.logstash.logback.decorate.JsonFactoryDecorator;

public class PhizJsonFactoryDecorator implements JsonFactoryDecorator {
    @Override
    public MappingJsonFactory decorate(MappingJsonFactory jsonFactory) {
        ObjectMapper objMapper = jsonFactory.getCodec();

        objMapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
        objMapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
        objMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        objMapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        objMapper.disable(MapperFeature.AUTO_DETECT_SETTERS);

        objMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        return jsonFactory;
    }
}
