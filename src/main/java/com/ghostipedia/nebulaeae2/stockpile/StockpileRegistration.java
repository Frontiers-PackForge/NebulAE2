package com.ghostipedia.nebulaeae2.stockpile;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.data.NebulaeDataGenerators;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.neoforged.bus.api.IEventBus;

public final class StockpileRegistration {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(NebulaeAE2.MODID, false);

    private StockpileRegistration() {}

    public static void init(IEventBus bus) {
        REGISTRATE.registerEventListeners(bus);
        NebulaeDataGenerators.init(REGISTRATE);
        REGISTRATE.defaultCreativeTab(GTCreativeModeTabs.MACHINE.getKey());
        GTMachineDefinitions.init();
    }
}
