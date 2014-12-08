package gov.hhs.onc.phiz.tools.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("toolTlsGenCreds")
public class TlsGenerateCredentialsTool extends AbstractPhizTool<TlsGenerateCredentialsToolOptions> {
    @Autowired
    public TlsGenerateCredentialsTool(TlsGenerateCredentialsToolOptions opts) {
        super(opts);
    }
}
