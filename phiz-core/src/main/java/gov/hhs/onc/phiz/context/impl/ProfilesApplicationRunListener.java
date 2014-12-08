package gov.hhs.onc.phiz.context.impl;

import gov.hhs.onc.phiz.context.PhizProfiles;
import gov.hhs.onc.phiz.context.PhizProperties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ProfilesApplicationRunListener extends AbstractPhizApplicationRunListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProfilesApplicationRunListener.class);

    public ProfilesApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment env) {
        if (env.containsProperty(PhizProperties.APP_NAME_NAME)) {
            String appName = env.getProperty(PhizProperties.APP_NAME_NAME), appProfileName =
                PhizProfiles.APP_PREFIX + StringUtils.join(ArrayUtils.subarray(StringUtils.split(appName, '-'), 1, Integer.MAX_VALUE), ".");

            env.addActiveProfile(appProfileName);

            LOGGER.info(String.format("Activated application (name=%s) Spring profile (name=%s).", appName, appProfileName));
        }

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
