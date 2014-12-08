package gov.hhs.onc.phiz.env.impl;

import gov.hhs.onc.phiz.context.impl.CommandLineApplicationRunListener;
import gov.hhs.onc.phiz.env.PhizCommandLineOptions;
import javax.annotation.Resource;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public abstract class AbstractPhizCommandLineOptions implements PhizCommandLineOptions {
    @Resource(name = CommandLineApplicationRunListener.ARGS_BEAN_NAME)
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    protected String[] args;
    
    protected ConfigurableEnvironment env;
    protected OptionParser parser;
    protected OptionSet set;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.parser = new OptionParser();
        
        // @formatter:off
        /*
        Arrays.stream(new BeanWrapperImpl(this).getPropertyDescriptors()).forEach(
            beanPropDesc -> {
                CommandLineOption optAnno = beanPropDesc.getWriteMethod().getAnnotation(CommandLineOption.class);
                OptionSpecBuilder optSpecBuilder = optParser.acceptsAll(Arrays.asList(optAnno.names()), optAnno.description());
            });
        */
        // @formatter:on
        
        this.set = this.parser.parse(this.args);
    }

    @Override
    public String[] getArguments() {
        return this.args;
    }
    
    @Override
    public void setEnvironment(Environment env) {
        this.env = ((ConfigurableEnvironment) env);
    }

    @Override
    public OptionParser getParser() {
        return this.parser;
    }

    @Override
    public OptionSet getSet() {
        return this.set;
    }
}
