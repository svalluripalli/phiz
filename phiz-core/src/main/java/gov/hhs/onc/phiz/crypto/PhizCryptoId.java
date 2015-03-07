package gov.hhs.onc.phiz.crypto;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

public interface PhizCryptoId extends PriorityOrdered {
    public String getId();

    @Override
    public default int getOrder() {
        return (this.getClass().isEnum() ? ((Enum<?>) this).ordinal() : Ordered.LOWEST_PRECEDENCE);
    }
}
