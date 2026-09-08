package com.ghostipedia.nebulaeae2.blocking;

public interface BlockingModeMenu {
    ProviderBlockingMode nebulae$blockingMode();
    void nebulae$cycleBlockingMode(boolean backwards);
}
