package com.ghostipedia.nebulaeae2.sticky;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import appeng.api.stacks.AEKey;

public interface StickyStorageConfiguration {
    void nebulae$configureSticky(Predicate<AEKey> claims, BooleanSupplier invalid);
    boolean nebulae$isStickyInvalid();
}
