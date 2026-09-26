package com.ghostipedia.nebulaeae2.p2p;

import java.util.UUID;

public interface PatternProviderRouteHost {
    UUID nebulae$providerIdentity();

    PatternProviderRoute nebulae$pendingRoute();

    void nebulae$setRoute(PatternProviderRoute route);
}
