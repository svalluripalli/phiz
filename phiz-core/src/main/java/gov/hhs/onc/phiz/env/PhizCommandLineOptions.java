package gov.hhs.onc.phiz.env;

import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

public interface PhizCommandLineOptions extends ApplicationContextAware, InitializingBean {
    public String[] getArguments();

    public boolean isHelp();

    public void setHelp(boolean help);

    public CmdLineParser getParser();
}
