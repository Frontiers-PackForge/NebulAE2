package com.ghostipedia.nebulaeae2.mixin.ae2.menu.optimizer;

import com.ghostipedia.nebulaeae2.optimizer.OptimizerMenu;
import com.ghostipedia.nebulaeae2.optimizer.OptimizerRequest;
import com.ghostipedia.nebulaeae2.optimizer.OptimizerSession;
import com.ghostipedia.nebulaeae2.optimizer.OptimizerSnapshot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;

@Mixin({CraftConfirmMenu.class, PatternAccessTermMenu.class})
public abstract class OptimizerMenuMixin extends AEBaseMenu implements OptimizerMenu {
    @Unique @GuiSync(750) public OptimizerSnapshot nebulae$optimizer = OptimizerSnapshot.EMPTY;
    @Unique private OptimizerSession nebulae$optimizerSession;

    protected OptimizerMenuMixin(MenuType<?> type, int id, Inventory inventory, Object host) {
        super(type, id, inventory, host);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$registerOptimizer(CallbackInfo callback) {
        if (nebulae$optimizerSession == null) {
            nebulae$optimizerSession = new OptimizerSession(this);
            registerClientAction("nebulaeOptimizer", OptimizerRequest.class, this::nebulae$optimizerAction);
        }
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$syncOptimizer(CallbackInfo callback) {
        if (isServerSide() && nebulae$optimizerSession != null) {
            nebulae$optimizerSession.tick();
            nebulae$optimizer = nebulae$optimizerSession.snapshot();
        }
    }

    @Override
    public OptimizerSnapshot nebulae$optimizerSnapshot() {
        return nebulae$optimizer;
    }

    @Override
    public void nebulae$optimizerAction(OptimizerRequest request) {
        if (isClientSide()) {
            sendClientAction("nebulaeOptimizer", request);
        } else if (nebulae$optimizerSession != null) {
            nebulae$optimizerSession.action(request);
        }
    }

    @Override
    public void nebulae$closeOptimizer() {
        if (nebulae$optimizerSession != null) {
            nebulae$optimizerSession.close();
        }
    }
}
