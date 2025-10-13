package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class CatamountPlayerDataAttachmentImpl {

    public static CatamountPlayerDataAttachment.Data getData(Player player) {
        return player.getAttachedOrCreate(TCDataAttachmentsFabric.CATAMOUNT_ATTACHMENT);
    }

    public static void setData(Player player, CatamountPlayerDataAttachment.Data data) {
        player.setAttached(TCDataAttachmentsFabric.CATAMOUNT_ATTACHMENT, data);
        sync(player, data);
    }

    public static void sync(Player player, CatamountPlayerDataAttachment.Data data) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new SyncCatamountPlayerDataPayload(player, data));
        }
    }
}
