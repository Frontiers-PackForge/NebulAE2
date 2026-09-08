package com.ghostipedia.nebulaeae2.blocking;

import java.util.Set;
import appeng.api.stacks.AEKey;

public interface BlockingTarget {
    boolean nebulae$blocks(ProviderBlockingMode mode, Set<AEKey> inputs);
}
