package com.ghostipedia.nebulaeae2.p2p;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public record PatternProviderRoute(UUID owner, PatternTunnelAddress provider, PatternTunnelAddress input,
        UUID inputIdentity, PatternTunnelAddress output, UUID outputIdentity, short frequency, boolean recovering) {
    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putUUID("owner", owner);
        tag.put("provider", provider.save());
        tag.put("input", input.save());
        tag.putUUID("inputIdentity", inputIdentity);
        tag.put("output", output.save());
        tag.putUUID("outputIdentity", outputIdentity);
        tag.putShort("frequency", frequency);
        tag.putBoolean("recovering", recovering);
        return tag;
    }

    public static PatternProviderRoute load(CompoundTag tag) {
        return new PatternProviderRoute(tag.getUUID("owner"), PatternTunnelAddress.load(tag.getCompound("provider")),
                PatternTunnelAddress.load(tag.getCompound("input")), tag.getUUID("inputIdentity"),
                PatternTunnelAddress.load(tag.getCompound("output")), tag.getUUID("outputIdentity"),
                tag.getShort("frequency"), tag.getBoolean("recovering"));
    }

    public PatternTunnelAddress destination() {
        return new PatternTunnelAddress(output.dimension(), output.position().relative(output.side()),
                output.side().getOpposite());
    }

    public PatternProviderRoute recover() {
        return new PatternProviderRoute(owner, provider, input, inputIdentity, output, outputIdentity, frequency, true);
    }
}
