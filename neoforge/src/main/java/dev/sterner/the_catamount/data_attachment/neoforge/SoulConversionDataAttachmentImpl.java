package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.SoulConversionDataAttachment;
import dev.sterner.the_catamount.neoforge.TCDataAttachmentsNeoForge;
import net.minecraft.server.level.ServerLevel;

public class SoulConversionDataAttachmentImpl {

    public static SoulConversionDataAttachment.Data getData(ServerLevel level) {
        return level.getData(TCDataAttachmentsNeoForge.SOUL_CONVERSION_ATTACHMENT);
    }

    public static void setData(ServerLevel level, SoulConversionDataAttachment.Data data) {
        level.setData(TCDataAttachmentsNeoForge.SOUL_CONVERSION_ATTACHMENT, data);
    }
}