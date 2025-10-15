package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.DangerousLeavesDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import net.minecraft.server.level.ServerLevel;

public class DangerousLeavesDataAttachmentImpl {

    public static DangerousLeavesDataAttachment.Data getData(ServerLevel level) {
        return level.getAttachedOrCreate(TCDataAttachmentsFabric.DANGEROUS_LEAVES_ATTACHMENT);
    }


    public static void setData(ServerLevel level, DangerousLeavesDataAttachment.Data data) {
        level.setAttached(TCDataAttachmentsFabric.DANGEROUS_LEAVES_ATTACHMENT, data);
    }
}
