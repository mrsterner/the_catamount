package dev.sterner.the_catamount.payload;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.client.ClientPaleAnimalTracker;
import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SyncPaleAnimalDataPayload implements CustomPacketPayload {

    private final CompoundTag nbt;

    public static final Type<SyncPaleAnimalDataPayload> ID =
            new Type<>(TheCatamount.id("sync_pale_animal_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPaleAnimalDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    SyncPaleAnimalDataPayload::write,
                    SyncPaleAnimalDataPayload::new
            );

    public SyncPaleAnimalDataPayload(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public SyncPaleAnimalDataPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public SyncPaleAnimalDataPayload(PaleAnimalDataAttachment.Data data) {
        CompoundTag tag = new CompoundTag();
        PaleAnimalDataAttachment.Data.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(TheCatamount.LOGGER::error)
                .ifPresent(encoded -> tag.put("paleAnimalData", encoded));
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
        CompoundTag dataTag = nbt.getCompound("paleAnimalData");

        Optional<PaleAnimalDataAttachment.Data> parsed = PaleAnimalDataAttachment.Data.CODEC
                .parse(NbtOps.INSTANCE, dataTag)
                .resultOrPartial(TheCatamount.LOGGER::error);

        client.execute(() -> {
            if (client.level != null) {
                parsed.ifPresent(data -> {
                    ClientPaleAnimalTracker.clear();
                    for (Map.Entry<UUID, Long> entry : data.paleAnimals().entrySet()) {
                        ClientPaleAnimalTracker.markPale(entry.getKey(), entry.getValue());
                    }
                });
            }
        });
    }
}