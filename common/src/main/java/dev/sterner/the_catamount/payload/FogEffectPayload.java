package dev.sterner.the_catamount.payload;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.ClientFogEffectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public class FogEffectPayload implements CustomPacketPayload {

    private final CompoundTag nbt;

    public static final Type<FogEffectPayload> ID =
            new Type<>(TheCatamount.id("fog_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FogEffectPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    FogEffectPayload::write,
                    FogEffectPayload::new
            );

    public FogEffectPayload(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public FogEffectPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public FogEffectPayload(Vec3 center, float radius, int duration) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("X", center.x);
        tag.putDouble("Y", center.y);
        tag.putDouble("Z", center.z);
        tag.putFloat("Radius", radius);
        tag.putInt("Duration", duration);
        this.nbt = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    public void handleS2C() {
        Minecraft client = Minecraft.getInstance();

        Vec3 center = new Vec3(
                nbt.getDouble("X"),
                nbt.getDouble("Y"),
                nbt.getDouble("Z")
        );
        float radius = nbt.getFloat("Radius");
        int duration = nbt.getInt("Duration");

        client.execute(() -> {
            ClientFogEffectTracker.setFogEffect(center, radius, duration);
        });
    }
}