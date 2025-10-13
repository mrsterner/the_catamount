package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsNeoForge;
import dev.sterner.the_catamount.payload.SyncCatamountPlayerDataPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class CatamountPlayerDataAttachmentImpl {

    public static CatamountPlayerDataAttachment.Data getData(Player player) {
        return player.getData(TCDataAttachmentsNeoForge.CATAMOUNT_ATTACHMENT);
    }

    public static void setData(Player player, CatamountPlayerDataAttachment.Data data) {
        player.setData(TCDataAttachmentsNeoForge.CATAMOUNT_ATTACHMENT, data);
        CatamountPlayerDataAttachment.sync(player, data);
    }

    public static void sync(Player player, CatamountPlayerDataAttachment.Data data) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCatamountPlayerDataPayload(player, data));
        }
    }
}
