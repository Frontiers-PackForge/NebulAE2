package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.crafting.CraftingSubmissionContext;
import com.ghostipedia.nebulaeae2.crafting.follow.FollowedCraftingMenu;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.menu.me.crafting.CraftConfirmMenu;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuFollowMixin implements FollowedCraftingMenu {
    @Unique private boolean nebulae$followed;

    @Override
    public void nebulae$setFollowed(boolean followed) {
        nebulae$followed = followed;
    }

    @WrapOperation(method = "startJob", at = @At(value = "INVOKE",
            target = "Lappeng/api/networking/crafting/ICraftingService;submitJob(Lappeng/api/networking/crafting/ICraftingPlan;Lappeng/api/networking/crafting/ICraftingRequester;Lappeng/api/networking/crafting/ICraftingCPU;ZLappeng/api/networking/security/IActionSource;)Lappeng/api/networking/crafting/ICraftingSubmitResult;"))
    private ICraftingSubmitResult nebulae$scopeFollow(ICraftingService service, ICraftingPlan plan,
            ICraftingRequester requester, ICraftingCPU cpu, boolean prioritizePower, IActionSource source,
            Operation<ICraftingSubmitResult> original) {
        Boolean previous = CraftingSubmissionContext.setFollowed(nebulae$followed);
        try {
            return original.call(service, plan, requester, cpu, prioritizePower, source);
        } finally {
            CraftingSubmissionContext.setFollowed(previous);
        }
    }
}
