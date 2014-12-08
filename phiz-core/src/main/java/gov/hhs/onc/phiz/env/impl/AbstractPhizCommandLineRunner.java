package gov.hhs.onc.phiz.env.impl;

import gov.hhs.onc.phiz.env.PhizCommandLineOptions;
import gov.hhs.onc.phiz.env.PhizCommandLineRunner;
import joptsimple.OptionSpec;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPhizCommandLineRunner<T extends PhizCommandLineOptions> implements PhizCommandLineRunner<T> {
    protected T opts;

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractPhizCommandLineRunner.class);
    
    protected AbstractPhizCommandLineRunner(T opts) {
        this.opts = opts;
    }

    @Override
    public void run(String ... args) throws Exception {
        // TEMP: dev
        StrBuilder optBuilder = new StrBuilder(this.getClass().getName());
        optBuilder.append(":");
        optBuilder.appendNewLine();
        optBuilder.appendWithSeparators(this.opts.getArguments(), ",");
        
        for (OptionSpec<?> optSpec : this.opts.getSet().specs()) {
            optBuilder.appendNewLine();
            optBuilder.appendWithSeparators(optSpec.options(), ",");
            optBuilder.append("=");
            
            if (this.opts.getSet().has(optSpec)) {
                optBuilder.appendWithSeparators(this.opts.getSet().valuesOf(optSpec), ",");
            } else {
                optBuilder.append("<unset>");
            }
        }
        
        LOGGER.warn(optBuilder.build());
    }
}
