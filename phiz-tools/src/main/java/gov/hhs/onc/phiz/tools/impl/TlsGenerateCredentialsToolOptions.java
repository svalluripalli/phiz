package gov.hhs.onc.phiz.tools.impl;

import gov.hhs.onc.phiz.context.PhizProfiles;
import org.kohsuke.args4j.Option;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("toolOptsTlsGenCreds")
@Profile({ (PhizProfiles.APP_PREFIX + "tools.tls.gen.creds") })
public class TlsGenerateCredentialsToolOptions extends AbstractPhizToolOptions {
    private String name;

    @Override
    public void afterPropertiesSet() throws Exception {
        // TEMP: dev
        //super.afterPropertiesSet();
    }

    public String getName() {
        return this.name;
    }

    @Option(name = "-n", aliases = { "--name" }, usage = "Credential name.", metaVar = "<name>", required = true)
    public void setName(String name) {
        this.name = name;
    }
}
