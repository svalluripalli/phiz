package gov.hhs.onc.phiz.tools.impl;

import gov.hhs.onc.phiz.env.CommandLineOption;
import gov.hhs.onc.phiz.env.CommandLineOptionArgument;
import org.springframework.stereotype.Component;

@Component("toolOptsTlsGenCreds")
public class TlsGenerateCredentialsToolOptions extends AbstractPhizToolOptions {
    @CommandLineOption(description = "Credential name.", names = { "n", "name" })
    @CommandLineOptionArgument
    public String getName() {
        return ((String) this.set.valueOf("name"));
    }
}
