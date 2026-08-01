package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui;

import appeng.init.client.InitScreens;
import appeng.menu.me.networktool.NetworkStatusMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InitScreens.class)
public abstract class InitScreensMixin {

    @ModifyArgs(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/init/client/InitScreens;register(Lnet/neoforged/neoforge/client/event/RegisterMenuScreensEvent;Lnet/minecraft/world/inventory/MenuType;Lappeng/init/client/InitScreens$StyledScreenFactory;Ljava/lang/String;)V"))
    private static void nebulae$useControllerComputeStyle(Args args) {
        if (args.get(1) == NetworkStatusMenu.CONTROLLER_TYPE) {
            args.set(3, "/screens/controller_compute.json");
        }
    }
}
