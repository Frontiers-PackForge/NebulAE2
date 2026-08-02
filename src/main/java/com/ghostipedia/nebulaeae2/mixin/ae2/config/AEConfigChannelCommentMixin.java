package com.ghostipedia.nebulaeae2.mixin.ae2.config;

import com.ghostipedia.nebulaeae2.config.AE2ChannelConfigPolicy;

import appeng.api.networking.pathing.ChannelMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "appeng.core.AEConfig$CommonConfig")
public abstract class AEConfigChannelCommentMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/AEConfig;defineEnum(Lnet/neoforged/neoforge/common/ModConfigSpec$Builder;Ljava/lang/String;Ljava/lang/Enum;Ljava/lang/String;)Lnet/neoforged/neoforge/common/ModConfigSpec$EnumValue;"),
            index = 3)
    private String nebulae$describeFrontiersChannelMode(String comment) {
        return AE2ChannelConfigPolicy.CHANNEL_COMMENT;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/AEConfig;defineEnum(Lnet/neoforged/neoforge/common/ModConfigSpec$Builder;Ljava/lang/String;Ljava/lang/Enum;Ljava/lang/String;)Lnet/neoforged/neoforge/common/ModConfigSpec$EnumValue;"),
            index = 2)
    private Enum<?> nebulae$defaultToFrontiersChannelMode(Enum<?> defaultValue) {
        return ChannelMode.X2;
    }
}
