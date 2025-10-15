package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import dev.sterner.the_catamount.payload.SyncPaleAnimalDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PaleAnimalDataAttachmentImpl {

    public static PaleAnimalDataAttachment.Data getData(ServerLevel level) {
        return level.getAttachedOrCreate(TCDataAttachmentsFabric.PALE_ANIMAL_ATTACHMENT);
    }

    public static void setData(ServerLevel level, PaleAnimalDataAttachment.Data data) {
        level.setAttached(TCDataAttachmentsFabric.PALE_ANIMAL_ATTACHMENT, data);
    }

    public static void sync(ServerLevel level, PaleAnimalDataAttachment.Data data, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ServerPlayNetworking.send(player, new SyncPaleAnimalDataPayload(data));
        }
    }
}