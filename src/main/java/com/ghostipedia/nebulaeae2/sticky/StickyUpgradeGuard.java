package com.ghostipedia.nebulaeae2.sticky;

import java.util.function.BooleanSupplier;

public interface StickyUpgradeGuard {
    void nebulae$setStickyInstallAllowed(BooleanSupplier allowed);
}
