package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;
import com.ghostipedia.nebulaeae2.crafting.CpuListTelemetry;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.guisync.GuiSync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingStatusMenu.class)
public abstract class CraftingStatusMenuTelemetryMixin implements ICpuTelemetryList {
    @Shadow @Final private WeakHashMap<ICraftingCPU, Integer> cpuSerialMap;
    @Unique @GuiSync(121) public CpuListTelemetry nebulae$cpuListTelemetry = new CpuListTelemetry(Map.of());

    @Inject(method = "createCpuList", at = @At("RETURN"))
    private void nebulae$captureCpuList(CallbackInfoReturnable<CraftingStatusMenu.CraftingCpuList> cir) {
        Map<Integer, CpuTelemetry> cpus = new HashMap<>();
        var currentSerials = new HashSet<Integer>();
        cir.getReturnValue().cpus().forEach(entry -> currentSerials.add(entry.serial()));
        cpuSerialMap.forEach((cpu, serial) -> {
            if (currentSerials.contains(serial) && cpu instanceof CraftingCPUCluster cluster) {
                cpus.put(serial, CpuTelemetry.capture(cluster));
            }
        });
        nebulae$cpuListTelemetry = new CpuListTelemetry(cpus);
    }

    @Override
    public CpuTelemetry nebulae$getCpuTelemetry(int serial) {
        return nebulae$cpuListTelemetry.cpus().getOrDefault(serial, CpuTelemetry.EMPTY);
    }
}
