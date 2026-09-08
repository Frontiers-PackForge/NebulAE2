package com.ghostipedia.nebulaeae2.mixin.ae2.menu.patternprovider;

import com.ghostipedia.nebulaeae2.blocking.BlockingModeMenu;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingMode;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingSettings;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderMenu.class)
public abstract class PatternProviderMenuBlockingMixin extends AEBaseMenu implements BlockingModeMenu {
    @Shadow @Final protected PatternProviderLogic logic;
    @Unique @GuiSync(731) public ProviderBlockingMode nebulae$blockingMode = ProviderBlockingMode.OFF;

    protected PatternProviderMenuBlockingMixin(MenuType<?> type, int id, Inventory inventory, Object host) {
        super(type, id, inventory, host);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$registerAction(CallbackInfo callback) {
        registerClientAction("nebulae_cycle_blocking", Boolean.class, backwards -> {
            if (backwards != null) {
                nebulae$cycleBlockingMode(backwards);
            }
        });
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$syncMode(CallbackInfo callback) {
        if (isServerSide()) {
            nebulae$blockingMode = logic.getConfigManager().getSetting(ProviderBlockingSettings.MODE);
        }
    }

    @Override
    public ProviderBlockingMode nebulae$blockingMode() {
        return nebulae$blockingMode;
    }

    @Override
    public void nebulae$cycleBlockingMode(boolean backwards) {
        if (isClientSide()) {
            sendClientAction("nebulae_cycle_blocking", backwards);
        } else {
            var config = logic.getConfigManager();
            config.putSetting(ProviderBlockingSettings.MODE, config.getSetting(ProviderBlockingSettings.MODE).next(backwards));
        }
    }
}
