package gov.hhs.onc.phiz.tools;

import gov.hhs.onc.phiz.env.CommandLineOption;
import gov.hhs.onc.phiz.env.PhizCommandLineOptions;

public interface PhizToolOptions extends PhizCommandLineOptions {
    @CommandLineOption(description = "Show help.", forHelp = true, names = { "h", "help" })
    public boolean isHelp();
}
