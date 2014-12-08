package gov.hhs.onc.phiz.env;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;

public interface PhizCommandLineOptions extends EnvironmentAware, InitializingBean {
    public String[] getArguments();

    public OptionParser getParser();

    public OptionSet getSet();
}
