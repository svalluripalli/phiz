package gov.hhs.onc.phiz.logging.impl;

import gov.hhs.onc.phiz.logging.MarkerFieldName;
import net.logstash.logback.marker.Markers;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

public final class PhizMarkers {
    public final static String MARKER_FIELD_NAME_DELIM = "_";

    private PhizMarkers() {
    }

    public static ObjectAppendingMarker append(Object ... markerObjs) {
        ObjectAppendingMarker marker = null, nextMarker;

        for (Object markerObj : markerObjs) {
            nextMarker = Markers.append(buildFieldName(markerObj), markerObj);

            marker = ((marker != null) ? marker.and(nextMarker) : nextMarker);
        }

        return marker;
    }

    public static String buildFieldName(Object markerObj) {
        Class<?> markerObjClass = markerObj.getClass();
        MarkerFieldName markerFieldNameAnno = AnnotationUtils.findAnnotation(markerObjClass, MarkerFieldName.class);
        String markerFieldName = ((markerFieldNameAnno != null) ? markerFieldNameAnno.value() : null);

        // noinspection ConstantConditions
        return StringUtils.join(
            StringUtils.splitByCharacterTypeCamelCase((!StringUtils.isEmpty(markerFieldName) ? markerFieldName : markerObjClass.getSimpleName())),
            MARKER_FIELD_NAME_DELIM).toLowerCase();
    }
}
