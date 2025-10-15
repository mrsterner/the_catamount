package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.PaleAnimalDataAttachment;
import dev.sterner.the_catamount.neoforge.TCDataAttachmentsNeoForge;
import dev.sterner.the_catamount.payload.SyncPaleAnimalDataPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PaleAnimalDataAttachmentImpl {

    public static PaleAnimalDataAttachment.Data getData(ServerLevel level) {
        return level.getData(TCDataAttachmentsNeoForge.PALE_ANIMAL_ATTACHMENT);
    }

    public static void setData(ServerLevel level, PaleAnimalDataAttachment.Data data) {
        level.setData(TCDataAttachmentsNeoForge.PALE_ANIMAL_ATTACHMENT, data);
    }

    public static void sync(ServerLevel level, PaleAnimalDataAttachment.Data data, List<ServerPlayer> players) {
        SyncPaleAnimalDataPayload payload = new SyncPaleAnimalDataPayload(data);
        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}