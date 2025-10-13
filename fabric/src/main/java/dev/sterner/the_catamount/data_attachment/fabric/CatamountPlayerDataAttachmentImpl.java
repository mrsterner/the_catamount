package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.fabric.TCDataAttachmentsFabric;
import net.minecraft.world.entity.player.Player;

public class CatamountPlayerDataAttachmentImpl {

    public static CatamountPlayerDataAttachment.Data getData(Player player) {
        return player.getAttached(TCDataAttachmentsFabric.CATAMOUNT_ATTACHMENT);
    }

    public static void setData(Player player, CatamountPlayerDataAttachment.Data data) {
        player.setAttached(TCDataAttachmentsFabric.CATAMOUNT_ATTACHMENT, data);
        CatamountPlayerDataAttachment.sync(player, data);
    }
}
