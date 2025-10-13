package dev.sterner.the_catamount.payload;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class SyncCatamountPlayerDataPayload implements CustomPacketPayload {

    private final CompoundTag nbt;

    public static final Type<SyncCatamountPlayerDataPayload> ID =
            new Type<>(TheCatamount.id("sync_catamount_player"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCatamountPlayerDataPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    SyncCatamountPlayerDataPayload::write,
                    SyncCatamountPlayerDataPayload::new
            );

    public SyncCatamountPlayerDataPayload(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public SyncCatamountPlayerDataPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public SyncCatamountPlayerDataPayload(Player player, CatamountPlayerDataAttachment.Data data) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", player.getUUID());
        CatamountPlayerDataAttachment.Data.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(TheCatamount.LOGGER::error)
                .ifPresent(encoded -> tag.put("catamountData", encoded));
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
        UUID id = nbt.getUUID("Id");
        CompoundTag dataTag = nbt.getCompound("catamountData");

        Optional<CatamountPlayerDataAttachment.Data> parsed = CatamountPlayerDataAttachment.Data.CODEC
                .parse(NbtOps.INSTANCE, dataTag)
                .resultOrPartial(TheCatamount.LOGGER::error);

        client.execute(() -> {
            if (client.level != null) {
                Player player = client.level.getPlayerByUUID(id);
                parsed.ifPresent(data -> {
                    if (player != null) {
                        CatamountPlayerDataAttachment.setData(player, data);
                    }
                });
            }
        });
    }
}
