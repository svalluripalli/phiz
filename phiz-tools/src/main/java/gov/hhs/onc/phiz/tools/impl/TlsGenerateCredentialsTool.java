package gov.hhs.onc.phiz.tools.impl;

import gov.hhs.onc.phiz.context.PhizProfiles;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("toolTlsGenCreds")
@Profile({ (PhizProfiles.APP_PREFIX + "tools.tls.gen.creds") })
public class TlsGenerateCredentialsTool extends AbstractPhizTool<TlsGenerateCredentialsToolOptions> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TlsGenerateCredentialsTool.class);

    @Autowired
    public TlsGenerateCredentialsTool(TlsGenerateCredentialsToolOptions opts) {
        super(opts);
    }

    @Override
    public void run(String ... args) throws Exception {
        // TEMP: dev
        LOGGER.info(StringUtils.join(args, " "));
    }
}
