package gov.hhs.onc.phiz.net.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component("convUri")
public class UriConverter implements Converter<String, URI> {
    @Override
    public URI convert(String src) {
        try {
            return new URI(src);
        } catch (URISyntaxException e) {
            throw new ConversionFailedException(TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(URI.class), src, e);
        }
    }
}
