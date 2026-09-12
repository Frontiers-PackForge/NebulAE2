package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryMenu;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.guisync.GuiSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingCPUMenu.class)
public abstract class CraftingCPUMenuTelemetryMixin implements ICpuTelemetryMenu {
    @Shadow private CraftingCPUCluster cpu;
    @Unique @GuiSync(120) public CpuTelemetry nebulae$cpuTelemetry = CpuTelemetry.EMPTY;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$captureTelemetry(CallbackInfo ci) {
        if (!((CraftingCPUMenu) (Object) this).getPlayer().level().isClientSide()) {
            nebulae$cpuTelemetry = CpuTelemetry.capture(cpu);
        }
    }

    @Override
    public CpuTelemetry nebulae$getCpuTelemetry() {
        return nebulae$cpuTelemetry;
    }
}
