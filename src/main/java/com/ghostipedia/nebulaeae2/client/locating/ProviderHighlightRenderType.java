package com.ghostipedia.nebulaeae2.client.locating;

import java.util.OptionalDouble;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public abstract class ProviderHighlightRenderType extends RenderType {
    public static final RenderType OUTLINE = create("nebulae_provider_outline", DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES, 1536, false, false, CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(2)))
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false));

    private ProviderHighlightRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int size,
            boolean crumbling, boolean sorting, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorting, setup, clear);
    }
}
