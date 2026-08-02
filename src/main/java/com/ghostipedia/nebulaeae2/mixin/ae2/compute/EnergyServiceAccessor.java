package com.ghostipedia.nebulaeae2.mixin.ae2.compute;

import java.util.List;

import appeng.me.Grid;
import appeng.me.service.EnergyService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnergyService.class)
public interface EnergyServiceAccessor {

    @Accessor("grid")
    Grid nebulae$getGrid();

    @Invoker("getConnectedServices")
    List<EnergyService> nebulae$getConnectedServices();
}
