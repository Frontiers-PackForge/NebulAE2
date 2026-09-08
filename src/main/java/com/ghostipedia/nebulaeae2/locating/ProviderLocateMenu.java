package com.ghostipedia.nebulaeae2.locating;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;

public interface ProviderLocateMenu {
    IGrid nebulae$locateGrid();
    boolean nebulae$canLocate(AEKey key);
}
