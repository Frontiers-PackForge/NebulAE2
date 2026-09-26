package com.ghostipedia.nebulaeae2;

import com.ghostipedia.nebulaeae2.compute.GridComputeService;
import com.ghostipedia.nebulaeae2.p2p.PatternP2PRegistration;
import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.config.AE2ChannelConfigPolicy;
import com.ghostipedia.nebulaeae2.controller.ControllerVisualStateSync;
import com.ghostipedia.nebulaeae2.crafting.follow.CraftingFollowNetworking;
import com.ghostipedia.nebulaeae2.locating.ProviderLocatingNetworking;
import com.ghostipedia.nebulaeae2.pattern.PatternAuthorship;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import appeng.api.networking.GridServices;
import org.slf4j.Logger;

@Mod(NebulaeAE2.MODID)
public final class NebulaeAE2 {

    public static final String MODID = "nebulaeae2";

    private static final Logger LOGGER = LogUtils.getLogger();

    public NebulaeAE2(IEventBus modBus, ModContainer modContainer) {
        AE2ChannelConfigPolicy.enforce();
        PatternP2PRegistration.init(modBus);
        StickyCard.init(modBus);
        GridServices.register(IComputeService.class, GridComputeService.class);
        ControllerVisualStateSync.init();
        PatternAuthorship.init(modBus);
        modBus.addListener(CraftingFollowNetworking::register);
        modBus.addListener(ProviderLocatingNetworking::register);
        LOGGER.info("Nebulae initialised - Beginning AE2 Interception");
    }
}
