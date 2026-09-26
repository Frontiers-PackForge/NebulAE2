package com.ghostipedia.nebulaeae2.stockpile;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import net.minecraft.network.chat.Component;

public final class GTMachineDefinitions {
    public static final MachineDefinition ME_STOCKPILE_TOPPER = StockpileRegistration.REGISTRATE
            .machine("me_stockpile_topper", info -> new MEStockpileTopperPartMachine(info, GTValues.HV))
            .langValue("ME Stockpile Topper Hatch")
            .tier(GTValues.HV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
            .colorOverlayTieredHullModel("me_stockpile_topper", null, "me_stockpile_topper_emissive")
            .tooltips(Component.translatable("tooltip.nebulaeae2.stockpile.description"),
                    Component.translatable("tooltip.nebulaeae2.stockpile.gap"),
                    Component.translatable("tooltip.nebulaeae2.stockpile.channel"),
                    Component.translatable("tooltip.nebulaeae2.stockpile.datastick"))
            .register();

    private GTMachineDefinitions() {}

    public static void init() {}
}
