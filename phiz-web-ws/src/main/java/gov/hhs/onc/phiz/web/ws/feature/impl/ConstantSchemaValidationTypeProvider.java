package gov.hhs.onc.phiz.web.ws.feature.impl;

import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.feature.validation.SchemaValidationTypeProvider;
import org.apache.cxf.service.model.OperationInfo;

public class ConstantSchemaValidationTypeProvider implements SchemaValidationTypeProvider {
    private SchemaValidationType type;

    public ConstantSchemaValidationTypeProvider(SchemaValidationType type) {
        this.type = type;
    }

    @Override
    public SchemaValidationType getSchemaValidationType(OperationInfo opInfo) {
        return this.type;
    }
}
