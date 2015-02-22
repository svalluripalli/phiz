package gov.hhs.onc.phiz.logging.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

@SuppressWarnings({ CompilerWarnings.SERIAL })
public class AtomicEventId extends AtomicLong {
    private final static LongUnaryOperator NEXT_UPDATE_OP = (value) -> ((value < Long.MAX_VALUE) ? ++value : 1);

    public long getNext() {
        return this.updateAndGet(NEXT_UPDATE_OP);
    }
}
