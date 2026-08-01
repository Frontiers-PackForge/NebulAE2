package com.ghostipedia.nebulaeae2;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NebulaeAE2.MODID)
public final class NebulaeAE2 {

    public static final String MODID = "nebulaeae2";

    private static final Logger LOGGER = LogUtils.getLogger();

    public NebulaeAE2(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("Nebulae initialised - Beginning AE2 Interception");
    }
}
