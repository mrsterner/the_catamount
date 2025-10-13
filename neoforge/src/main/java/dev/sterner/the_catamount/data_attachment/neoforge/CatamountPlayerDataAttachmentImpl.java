package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.neoforge.TCDataAttachmentsNeoForge;
import net.minecraft.world.entity.player.Player;

public class CatamountPlayerDataAttachmentImpl {

    public static CatamountPlayerDataAttachment.Data getData(Player player) {
        return player.getData(TCDataAttachmentsNeoForge.CATAMOUNT_ATTACHMENT);
    }

    public static void setData(Player player, CatamountPlayerDataAttachment.Data data) {
        player.setData(TCDataAttachmentsNeoForge.CATAMOUNT_ATTACHMENT, data);
        CatamountPlayerDataAttachment.sync(player, data);
    }
}
