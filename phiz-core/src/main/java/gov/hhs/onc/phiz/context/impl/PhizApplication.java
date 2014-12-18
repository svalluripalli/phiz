package gov.hhs.onc.phiz.context.impl;

import java.util.stream.Collectors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ "classpath*:META-INF/phiz/spring/spring-phiz*.xml" })
public abstract class PhizApplication {
    public static void main(String ... args) {
        buildApplication().run(args);
    }

    public static SpringApplication buildApplication() {
        SpringApplication app =
            new SpringApplicationBuilder(PhizApplication.class).addCommandLineProperties(false).showBanner(false).headless(true).application();
        app.setListeners(app.getListeners().stream().filter((appListener -> !appListener.getClass().equals(LoggingApplicationListener.class)))
            .collect(Collectors.toList()));

        return app;
    }
}
