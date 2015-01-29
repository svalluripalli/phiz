package gov.hhs.onc.phiz.logging.logstash.impl;

import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.core.annotation.AnnotationUtils;

public final class PhizLogstashMarkers {
    public final static String MARKER_FIELD_NAME_DELIM = "_";

    private PhizLogstashMarkers() {
    }

    public static Marker append(Object ... markerObjs) {
        Marker marker = null, nextMarker;

        for (Object markerObj : markerObjs) {
            if (markerObj instanceof Marker) {
                nextMarker = ((Marker) markerObj);
            } else if (markerObj instanceof String) {
                nextMarker = MarkerFactory.getMarker(((String) markerObj));
            } else {
                nextMarker = Markers.append(buildFieldName(markerObj), markerObj);
            }

            if (marker != null) {
                marker.add(nextMarker);
            } else {
                marker = nextMarker;
            }
        }

        return marker;
    }

    public static String buildFieldName(Object markerObj) {
        Class<?> markerObjClass = markerObj.getClass();
        MarkerObjectFieldName markerObjFieldNameAnno = AnnotationUtils.findAnnotation(markerObjClass, MarkerObjectFieldName.class);
        String markerFieldName = ((markerObjFieldNameAnno != null) ? markerObjFieldNameAnno.value() : null);

        return buildFieldName((!StringUtils.isEmpty(markerFieldName) ? markerFieldName : markerObjClass.getSimpleName()));
    }

    public static String buildFieldName(String markerFieldName) {
        // noinspection ConstantConditions
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(markerFieldName), MARKER_FIELD_NAME_DELIM).toLowerCase();
    }
}
