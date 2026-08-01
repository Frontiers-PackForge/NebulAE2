package com.ghostipedia.nebulaeae2.mixin.ae2.client.render.cablebus;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import java.util.EnumSet;
import java.util.List;

import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.thirdparty.fabric.ColorHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "appeng.client.render.cablebus.CableBuilder")
public abstract class CableBuilderOverloadColorMixin {

    @Unique
    private static final int nebulae$CHANNEL_MASK = 0x3F;

    @Unique
    private static final int nebulae$DENSE_DIVIDED_CHANNEL_MASK = 0x0F;

    @Unique
    private static final int nebulae$STANDARD_OVERLOAD_SHIFT = 6;

    @Unique
    private static final int nebulae$STANDARD_OPPOSITE_OVERLOAD_SHIFT = 14;

    @Unique
    private static final int nebulae$DENSE_OVERLOAD_SHIFT = 4;

    @Unique
    private static final int nebulae$DENSE_OPPOSITE_OVERLOAD_SHIFT = 12;

    @ModifyArg(
            method = {
                    "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
                    "addConstrainedSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;IILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/SmartCableTextures;getOddTextureForChannels(I)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"),
            index = 0,
            require = 3)
    private int nebulae$decodeSmartOddTextureChannels(int encodedChannels) {
        return encodedChannels & nebulae$CHANNEL_MASK;
    }

    @ModifyArg(
            method = {
                    "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
                    "addConstrainedSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;IILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/SmartCableTextures;getEvenTextureForChannels(I)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"),
            index = 0,
            require = 3)
    private int nebulae$decodeSmartEvenTextureChannels(int encodedChannels) {
        return encodedChannels & nebulae$CHANNEL_MASK;
    }

    @ModifyArg(
            method = {
                    "addDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/SmartCableTextures;getOddTextureForDenseChannels(I)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"),
            index = 0,
            require = 2)
    private int nebulae$decodeDenseOddTextureChannels(int dividedEncodedChannels) {
        return dividedEncodedChannels & nebulae$DENSE_DIVIDED_CHANNEL_MASK;
    }

    @ModifyArg(
            method = {
                    "addDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/SmartCableTextures;getEvenTextureForDenseChannels(I)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"),
            index = 0,
            require = 2)
    private int nebulae$decodeDenseEvenTextureChannels(int dividedEncodedChannels) {
        return dividedEncodedChannels & nebulae$DENSE_DIVIDED_CHANNEL_MASK;
    }

    @ModifyArg(
            method = "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 0),
            index = 0,
            require = 1)
    private int nebulae$colorSmartEndCapOdd(int nativeColor,
                                            @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, false);
    }

    @ModifyArg(
            method = "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 1),
            index = 0,
            require = 1)
    private int nebulae$colorSmartEndCapEven(int nativeColor,
                                             @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, true);
    }

    @ModifyArg(
            method = "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 2),
            index = 0,
            require = 1)
    private int nebulae$colorSmartArmOdd(int nativeColor, @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, false);
    }

    @ModifyArg(
            method = "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 3),
            index = 0,
            require = 1)
    private int nebulae$colorSmartArmEven(int nativeColor, @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, true);
    }

    @ModifyArg(
            method = "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 0),
            index = 0,
            require = 1)
    private int nebulae$colorStraightSmartOdd(int nativeColor,
                                              @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, false);
    }

    @ModifyArg(
            method = "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 1),
            index = 0,
            require = 1)
    private int nebulae$colorStraightSmartEven(int nativeColor,
                                               @Local(argsOnly = true, ordinal = 0) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, true);
    }

    @Inject(
            method = {
                    "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
                    "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"
            },
            at = @At("HEAD"),
            require = 2)
    private void nebulae$captureStraightQuadStart(Direction facing, AEColor cableColor, int encodedChannels,
                                                   List<BakedQuad> quadsOut, CallbackInfo ci,
                                                   @Share("nebulae$straightQuadStart") LocalIntRef quadStart) {
        quadStart.set(quadsOut.size());
    }

    @Inject(
            method = "addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
            at = @At("RETURN"),
            require = 1)
    private void nebulae$gradientStraightSmartConnection(Direction facing, AEColor cableColor, int encodedChannels,
                                                          List<BakedQuad> quadsOut, CallbackInfo ci,
                                                          @Share("nebulae$straightQuadStart") LocalIntRef quadStart) {
        nebulae$gradientStraightConnection(facing, cableColor, encodedChannels, quadsOut, quadStart.get(),
                nebulae$STANDARD_OVERLOAD_SHIFT, nebulae$STANDARD_OPPOSITE_OVERLOAD_SHIFT);
    }

    @Inject(
            method = "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V",
            at = @At("RETURN"),
            require = 1)
    private void nebulae$gradientStraightDenseSmartConnection(Direction facing, AEColor cableColor,
                                                               int dividedEncodedChannels,
                                                               List<BakedQuad> quadsOut, CallbackInfo ci,
                                                               @Share("nebulae$straightQuadStart") LocalIntRef quadStart) {
        nebulae$gradientStraightConnection(facing, cableColor, dividedEncodedChannels, quadsOut, quadStart.get(),
                nebulae$DENSE_OVERLOAD_SHIFT, nebulae$DENSE_OPPOSITE_OVERLOAD_SHIFT);
    }

    @ModifyArg(
            method = "addConstrainedSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;IILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 0),
            index = 0,
            require = 1)
    private int nebulae$colorConstrainedSmartOdd(int nativeColor,
                                                 @Local(argsOnly = true, ordinal = 1) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, false);
    }

    @ModifyArg(
            method = "addConstrainedSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;IILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 1),
            index = 0,
            require = 1)
    private int nebulae$colorConstrainedSmartEven(int nativeColor,
                                                  @Local(argsOnly = true, ordinal = 1) int encodedChannels) {
        return nebulae$overloadColor(nativeColor, encodedChannels, 6, true);
    }

    @ModifyArg(
            method = {
                    "addDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 0),
            index = 0,
            require = 2)
    private int nebulae$colorDenseSmartOdd(int nativeColor,
                                           @Local(argsOnly = true, ordinal = 0) int dividedEncodedChannels) {
        return nebulae$overloadColor(nativeColor, dividedEncodedChannels, 4, false);
    }

    @ModifyArg(
            method = {
                    "addDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setColorRGB(I)V",
                    ordinal = 1),
            index = 0,
            require = 2)
    private int nebulae$colorDenseSmartEven(int nativeColor,
                                            @Local(argsOnly = true, ordinal = 0) int dividedEncodedChannels) {
        return nebulae$overloadColor(nativeColor, dividedEncodedChannels, 4, true);
    }

    @ModifyArg(
            method = {
                    "addSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V",
                    "addDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;Lappeng/api/util/AECableType;ZILjava/util/List;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setEmissiveMaterial(Z)V"),
            index = 0,
            require = 6)
    private boolean nebulae$limitConnectionIndicatorFaces(boolean emissive,
                                                           @Local CubeBuilder cubeBuilder,
                                                           @Local(argsOnly = true) Direction facing,
                                                           @Local(argsOnly = true) AEColor cableColor) {
        cubeBuilder.setDrawFaces(emissive
                ? EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))
                : EnumSet.complementOf(EnumSet.of(facing)));
        if (!emissive) {
            cubeBuilder.setColorRGB(cableColor.whiteVariant);
        }
        return emissive;
    }

    @ModifyArg(
            method = "addConstrainedSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;IILjava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CubeBuilder;setEmissiveMaterial(Z)V"),
            index = 0,
            require = 2)
    private boolean nebulae$limitConstrainedIndicatorFaces(boolean emissive,
                                                            @Local CubeBuilder cubeBuilder,
                                                            @Local(argsOnly = true) Direction facing) {
        cubeBuilder.setDrawFaces(emissive
                ? EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))
                : EnumSet.allOf(Direction.class));
        return emissive;
    }

    @Unique
    private static int nebulae$overloadColor(int nativeColor, int encodedChannels, int bandShift, boolean bright) {
        int overloadBand = encodedChannels >>> bandShift & 0x03;
        return nebulae$overloadColor(nativeColor, overloadBand, bright);
    }

    @Unique
    private static int nebulae$overloadColor(int nativeColor, int overloadBand, boolean bright) {
        return switch (overloadBand) {
            case 1 -> bright ? 0xFFB000 : 0xD66A00;
            case 2 -> bright ? 0xFF5A00 : 0xC43800;
            case 3 -> bright ? 0xFF2020 : 0xA00000;
            default -> nativeColor;
        };
    }

    @Unique
    private static void nebulae$gradientStraightConnection(Direction facing, AEColor cableColor, int encodedChannels,
                                                            List<BakedQuad> quadsOut, int quadStart, int bandShift,
                                                            int oppositeBandShift) {
        int facingBand = encodedChannels >>> bandShift & 0x03;
        int oppositeBand = encodedChannels >>> oppositeBandShift & 0x03;
        int addedQuads = quadsOut.size() - quadStart;
        if (addedQuads % 3 != 0) {
            return;
        }

        int quadsPerLayer = addedQuads / 3;
        if (facingBand != oppositeBand) {
            nebulae$gradientLayer(facing, quadsOut, quadStart + quadsPerLayer, quadsPerLayer,
                    nebulae$overloadColor(cableColor.blackVariant, facingBand, false),
                    nebulae$overloadColor(cableColor.blackVariant, oppositeBand, false));
            nebulae$gradientLayer(facing, quadsOut, quadStart + quadsPerLayer * 2, quadsPerLayer,
                    nebulae$overloadColor(cableColor.whiteVariant, facingBand, true),
                    nebulae$overloadColor(cableColor.whiteVariant, oppositeBand, true));
        }
        nebulae$removeAxialIndicatorFaces(facing, quadsOut, quadStart, quadsPerLayer);
    }

    @Unique
    private static void nebulae$removeAxialIndicatorFaces(Direction facing, List<BakedQuad> quadsOut, int quadStart,
                                                           int quadsPerLayer) {
        int firstIndicatorQuad = quadStart + quadsPerLayer;
        int end = quadStart + quadsPerLayer * 3;
        for (int quadIndex = end - 1; quadIndex >= firstIndicatorQuad; quadIndex--) {
            if (quadsOut.get(quadIndex).getDirection().getAxis() == facing.getAxis()) {
                quadsOut.remove(quadIndex);
            }
        }
    }

    @Unique
    private static void nebulae$gradientLayer(Direction facing, List<BakedQuad> quadsOut, int firstQuad,
                                               int quadCount, int facingColor, int oppositeColor) {
        int coordinateOffset = switch (facing.getAxis()) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
        boolean facingPositive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;

        for (int quadIndex = firstQuad; quadIndex < firstQuad + quadCount; quadIndex++) {
            int[] vertices = quadsOut.get(quadIndex).getVertices();
            int vertexStride = vertices.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int vertexOffset = vertex * vertexStride;
                float coordinate = Float.intBitsToFloat(vertices[vertexOffset + coordinateOffset]);
                float blend = facingPositive ? 1.0f - coordinate : coordinate;
                vertices[vertexOffset + 3] = ColorHelper.toVanillaColor(
                        nebulae$lerpColor(facingColor, oppositeColor, Math.clamp(blend, 0.0f, 1.0f)));
            }
        }
    }

    @Unique
    private static int nebulae$lerpColor(int first, int second, float amount) {
        int red = Math.round((first >> 16 & 0xFF) + ((second >> 16 & 0xFF) - (first >> 16 & 0xFF)) * amount);
        int green = Math.round((first >> 8 & 0xFF) + ((second >> 8 & 0xFF) - (first >> 8 & 0xFF)) * amount);
        int blue = Math.round((first & 0xFF) + ((second & 0xFF) - (first & 0xFF)) * amount);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
