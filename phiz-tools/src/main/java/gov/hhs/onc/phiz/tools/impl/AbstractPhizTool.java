package gov.hhs.onc.phiz.tools.impl;

import gov.hhs.onc.phiz.env.impl.AbstractPhizCommandLineRunner;
import gov.hhs.onc.phiz.tools.PhizTool;
import gov.hhs.onc.phiz.tools.PhizToolOptions;

public abstract class AbstractPhizTool<T extends PhizToolOptions> extends AbstractPhizCommandLineRunner<T> implements PhizTool<T> {
    protected AbstractPhizTool(T opts) {
        super(opts);
    }
}
