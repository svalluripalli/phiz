package gov.hhs.onc.phiz.env.impl;

import gov.hhs.onc.phiz.env.PhizCommandLineOptions;
import gov.hhs.onc.phiz.env.PhizCommandLineRunner;

public abstract class AbstractPhizCommandLineRunner<T extends PhizCommandLineOptions> implements PhizCommandLineRunner<T> {
    protected T opts;

    protected AbstractPhizCommandLineRunner(T opts) {
        this.opts = opts;
    }
}
