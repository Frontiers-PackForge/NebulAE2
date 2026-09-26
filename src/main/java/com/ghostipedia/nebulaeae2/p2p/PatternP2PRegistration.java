package com.ghostipedia.nebulaeae2.p2p;

import com.ghostipedia.nebulaeae2.NebulaeAE2;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PatternP2PRegistration {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NebulaeAE2.MODID);
    public static final DeferredItem<PartItem<PatternP2PTunnelPart>> PATTERN_P2P = ITEMS.register("pattern_p2p_tunnel",
            () -> new PartItem<>(new Item.Properties(), PatternP2PTunnelPart.class, PatternP2PTunnelPart::new));

    private PatternP2PRegistration() {}

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(PatternP2PRegistration::creativeTab);
        NeoForge.EVENT_BUS.addListener(PatternP2PRegistration::tooltip);
        for (var model : PatternP2PTunnelPart.getModels()) {
            PartModels.registerModels(model.getModels());
        }
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(PATTERN_P2P);
        }
    }

    private static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(PATTERN_P2P.get())) {
            event.getToolTip().add(Component.translatable("tooltip.nebulaeae2.pattern_p2p.processing"));
            event.getToolTip().add(Component.translatable("tooltip.nebulaeae2.pattern_p2p.join_input"));
        }
    }
}
