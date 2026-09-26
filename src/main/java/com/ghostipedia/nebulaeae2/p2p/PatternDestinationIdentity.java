package com.ghostipedia.nebulaeae2.p2p;

import java.util.List;

public record PatternDestinationIdentity(PatternTunnelAddress address, List<Object> capabilities) {
    public PatternDestinationIdentity {
        capabilities = List.copyOf(capabilities);
    }

    public boolean sharesInventory(PatternDestinationIdentity other) {
        if (!address.dimension().equals(other.address.dimension())) {
            return false;
        }
        if (address.position().equals(other.address.position())) {
            return true;
        }
        for (var first : capabilities) {
            for (var second : other.capabilities) {
                if (first == second) {
                    return true;
                }
            }
        }
        return false;
    }
}
