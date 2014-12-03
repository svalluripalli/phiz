package gov.hhs.onc.phiz.context.impl;

import gov.hhs.onc.phiz.context.config.PhizProfiles;
import gov.hhs.onc.phiz.context.config.PhizProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.env.ConfigurableEnvironment;

public class ProfilesApplicationRunListener extends AbstractPhizApplicationRunListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProfilesApplicationRunListener.class);

    public ProfilesApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment env) {
        String modeName = env.getProperty(PhizProperties.MODE_NAME, PhizProperties.PROD_MODE_VALUE).toLowerCase(), modeProfileName;

        switch (modeName) {
            case PhizProperties.DEV_MODE_VALUE:
                modeProfileName = PhizProfiles.DEV_MODE;
                break;

            case PhizProperties.PROD_MODE_VALUE:
                modeProfileName = PhizProfiles.PROD_MODE;
                break;

            default:
                throw new ApplicationContextException(String.format("Unknown mode (name=%s).", modeName));
        }

        env.addActiveProfile(modeProfileName);

        LOGGER.info(String.format("Activated mode (name=%s) Spring profile (name=%s).", modeName, modeProfileName));
    }
}
