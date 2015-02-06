package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.IncludeAction;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import java.net.URL;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class IncludesAction extends Action {
    private final static String INCLUDE_ACTION_NAME = "include";

    private final static String FILE_ATTR_NAME = "file";
    private final static String OPTIONAL_ATTR_NAME = "optional";
    private final static String RESOURCE_ATTR_NAME = "resource";
    private final static String RESOURCES_ATTR_NAME = "resources";
    private final static String URL_ATTR_NAME = "url";

    @Override
    public void end(InterpretationContext interpretationContext, String name) throws ActionException {
    }

    @Override
    public void begin(InterpretationContext interpretationContext, String name, Attributes attrs) throws ActionException {
        String resourcesAttrValue = attrs.getValue(RESOURCES_ATTR_NAME);

        if (StringUtils.isBlank(resourcesAttrValue)) {
            this.addError(String.format("Resources attribute value is blank: %s", resourcesAttrValue));

            return;
        }

        AttributesImpl includeAttrsBase = new AttributesImpl(), includeAttrs;
        includeAttrsBase.addAttribute(StringUtils.EMPTY, OPTIONAL_ATTR_NAME, OPTIONAL_ATTR_NAME, null,
            ObjectUtils.defaultIfNull(attrs.getValue(OPTIONAL_ATTR_NAME), Boolean.toString(false)));

        IncludeAction includeAction = new IncludeAction();
        includeAction.setContext(this.context);

        URL resourceUrl;

        try {
            for (Resource resource : ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader()).getResources(
                StringUtils.prependIfMissing(resourcesAttrValue, ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX))) {
                includeAttrs = new AttributesImpl(includeAttrsBase);

                if (resource instanceof ClassPathResource) {
                    includeAttrs.addAttribute(StringUtils.EMPTY, RESOURCE_ATTR_NAME, RESOURCE_ATTR_NAME, null, ((ClassPathResource) resource).getPath());
                } else if (resource instanceof FileSystemResource) {
                    includeAttrs.addAttribute(StringUtils.EMPTY, FILE_ATTR_NAME, FILE_ATTR_NAME, null, resource.getFile().getPath());
                } else if (ResourceUtils.isJarURL((resourceUrl = resource.getURL()))) {
                    // noinspection ConstantConditions
                    includeAttrs.addAttribute(StringUtils.EMPTY, RESOURCE_ATTR_NAME, RESOURCE_ATTR_NAME, null,
                        StringUtils.splitByWholeSeparator(resourceUrl.toString(), ResourceUtils.JAR_URL_SEPARATOR, 2)[1]);
                } else {
                    includeAttrs.addAttribute(StringUtils.EMPTY, URL_ATTR_NAME, URL_ATTR_NAME, null, resourceUrl.toString());
                }

                includeAction.begin(interpretationContext, INCLUDE_ACTION_NAME, includeAttrs);
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }
}
