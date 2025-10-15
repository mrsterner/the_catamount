package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.DangerousLeavesDataAttachment;
import dev.sterner.the_catamount.neoforge.TCDataAttachmentsNeoForge;
import net.minecraft.server.level.ServerLevel;

public class DangerousLeavesDataAttachmentImpl {

    public static DangerousLeavesDataAttachment.Data getData(ServerLevel level) {
        return level.getData(TCDataAttachmentsNeoForge.DANGEROUS_LEAVES_ATTACHMENT);
    }

    public static void setData(ServerLevel level, DangerousLeavesDataAttachment.Data data) {
        level.setData(TCDataAttachmentsNeoForge.DANGEROUS_LEAVES_ATTACHMENT, data);
    }
}
