package gov.hhs.onc.phiz.crypto.logging;

import gov.hhs.onc.phiz.crypto.PhizCryptoTagId;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum SslDebugPrintStreamType implements PhizCryptoTagId {
    OUT(1, () -> System.out, System::setOut), ERR(2, () -> System.err, System::setErr);

    private final int tag;
    private final String id;
    private final Supplier<PrintStream> getter;
    private final Consumer<PrintStream> setter;

    private SslDebugPrintStreamType(int tag, Supplier<PrintStream> getter, Consumer<PrintStream> setter) {
        this.tag = tag;
        this.id = this.name().toLowerCase();
        this.getter = getter;
        this.setter = setter;
    }

    public Supplier<PrintStream> getGetter() {
        return this.getter;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Consumer<PrintStream> getSetter() {
        return this.setter;
    }

    @Override
    public int getTag() {
        return this.tag;
    }
}
