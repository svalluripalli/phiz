package gov.hhs.onc.phiz.destination;

import javax.annotation.Nullable;

public interface PhizDestinationRegistry {
    @Nullable
    public PhizDestination findById(String id);
}
