package com.ghostipedia.nebulaeae2.p2p;

public enum PatternRouteAvailability {
    WAIT,
    DELIVER,
    RECOVER;

    public enum Endpoint {
        UNLOADED,
        REMOVED,
        INACTIVE,
        ACTIVE
    }

    public static PatternRouteAvailability resolve(Endpoint input, Endpoint output, boolean connected) {
        if (input == Endpoint.REMOVED || output == Endpoint.REMOVED) {
            return RECOVER;
        }
        return input == Endpoint.ACTIVE && output == Endpoint.ACTIVE && connected ? DELIVER : WAIT;
    }
}
