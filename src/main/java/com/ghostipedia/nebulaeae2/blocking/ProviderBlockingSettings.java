package com.ghostipedia.nebulaeae2.blocking;

import appeng.api.config.Setting;

public final class ProviderBlockingSettings {
    public static final Setting<ProviderBlockingMode> MODE = new Setting<>("nebulae_blocking_mode", ProviderBlockingMode.class);

    private ProviderBlockingSettings() {}
}
