package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;
import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import com.ghostipedia.nebulaeae2.crafting.CraftingCpuSelection;
import com.ghostipedia.nebulaeae2.crafting.CraftingAdmissionFailure;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryMenu;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.guisync.GuiSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuTelemetryMixin implements ICpuTelemetryMenu {
    @Shadow private ICraftingCPU selectedCpu;
    @Shadow private ICraftingPlan result;
    @Shadow private IGrid getGrid() { throw new AssertionError(); }
    @Shadow private IActionSource getActionSrc() { throw new AssertionError(); }
    @Unique @GuiSync(120) public CpuTelemetry nebulae$cpuTelemetry = CpuTelemetry.EMPTY;
    @Unique @GuiSync(122) public boolean nebulae$admissionFailed;

    @WrapOperation(method = "startJob", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingService;submitJob(Lappeng/api/networking/crafting/ICraftingPlan;Lappeng/api/networking/crafting/ICraftingRequester;Lappeng/api/networking/crafting/ICraftingCPU;ZLappeng/api/networking/security/IActionSource;)Lappeng/api/networking/crafting/ICraftingSubmitResult;"))
    private ICraftingSubmitResult nebulae$trackAdmission(ICraftingService service, ICraftingPlan plan,
            ICraftingRequester requester, ICraftingCPU cpu, boolean prioritizePower, IActionSource source,
            Operation<ICraftingSubmitResult> original) {
        var submission = original.call(service, plan, requester, cpu, prioritizePower, source);
        nebulae$admissionFailed = submission == CraftingAdmissionFailure.INSUFFICIENT_CWU;
        return submission;
    }

    @Override
    public boolean nebulae$admissionFailed() {
        return nebulae$admissionFailed;
    }

    @Inject(method = "broadcastChanges", at = @At(value = "INVOKE", target = "Lappeng/menu/AEBaseMenu;broadcastChanges()V"))
    private void nebulae$captureQuote(CallbackInfo ci) {
        var grid = getGrid();
        if (grid == null || result == null) {
            nebulae$cpuTelemetry = CpuTelemetry.EMPTY;
            return;
        }
        var snapshot = grid.getService(IComputeService.class).snapshot();
        CraftingCPUCluster cpu = selectedCpu instanceof CraftingCPUCluster cluster ? cluster
                : CraftingCpuSelection.select(grid, result, getActionSrc(), true);
        if (cpu == null) {
            nebulae$cpuTelemetry = new CpuTelemetry(0, 0, 0, 0, snapshot.availableCraftingCwut(), snapshot.craftingPaused(), false);
            return;
        }
        var data = CpuTelemetry.capture(cpu);
        long cost = CraftingComputeTuning.jobReservationCwut(result.bytes(), data.acceleration(), data.parallel());
        nebulae$cpuTelemetry = new CpuTelemetry(data.acceleration(), data.parallel(), data.storage(), cost,
                snapshot.availableCraftingCwut(), snapshot.craftingPaused() || cost > snapshot.availableCraftingCwut(), true);
    }

    @Override
    public CpuTelemetry nebulae$getCpuTelemetry() {
        return nebulae$cpuTelemetry;
    }
}
