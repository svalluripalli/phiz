package gov.hhs.onc.phiz.env.impl;

import gov.hhs.onc.phiz.env.PhizCommandLineOptions;
import java.io.StringWriter;
import javax.annotation.Resource;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public abstract class AbstractPhizCommandLineOptions implements PhizCommandLineOptions {
    @Resource(name = CommandLineApplicationRunListener.ARGS_BEAN_NAME)
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    protected String[] args;

    protected ApplicationContext appContext;
    protected CmdLineParser parser;
    protected boolean help;

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractPhizCommandLineOptions.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        this.parser = new CmdLineParser(this);

        try {
            this.parser.parseArgument(this.args);
        } catch (CmdLineException e) {
            LOGGER.error("Unable to parse command line option(s).", e);

            SpringApplication.exit(this.appContext, (() -> 1));
        }

        if (this.help) {
            StringWriter usageWriter = new StringWriter();

            this.parser.printUsage(usageWriter, null);

            LOGGER.info(usageWriter.toString());

            SpringApplication.exit(this.appContext, (() -> 0));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }

    @Override
    public String[] getArguments() {
        return this.args;
    }

    @Override
    public boolean isHelp() {
        return this.help;
    }

    @Option(name = "--help", usage = "Show help.", help = true)
    @Override
    public void setHelp(boolean help) {
        this.help = help;
    }

    @Override
    public CmdLineParser getParser() {
        return this.parser;
    }
}
