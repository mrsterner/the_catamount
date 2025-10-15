package dev.sterner.the_catamount.payload;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.ClientPaleAnimalTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public class PaleAnimalSyncPayload implements CustomPacketPayload {

    private final CompoundTag nbt;

    public static final Type<PaleAnimalSyncPayload> ID =
            new Type<>(TheCatamount.id("sync_pale_animal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaleAnimalSyncPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    PaleAnimalSyncPayload::write,
                    PaleAnimalSyncPayload::new
            );

    public PaleAnimalSyncPayload(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public PaleAnimalSyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public PaleAnimalSyncPayload(UUID animalUUID, long endTime, boolean remove) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("AnimalUUID", animalUUID);
        tag.putLong("EndTime", endTime);
        tag.putBoolean("Remove", remove);
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
        UUID animalUUID = nbt.getUUID("AnimalUUID");
        long endTime = nbt.getLong("EndTime");
        boolean remove = nbt.getBoolean("Remove");

        client.execute(() -> {
            if (remove) {
                ClientPaleAnimalTracker.removePale(animalUUID);
            } else {
                ClientPaleAnimalTracker.markPale(animalUUID, endTime);
            }
        });
    }
}