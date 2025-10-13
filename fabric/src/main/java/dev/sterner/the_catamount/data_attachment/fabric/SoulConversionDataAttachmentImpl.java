package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.SoulConversionDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import net.minecraft.server.level.ServerLevel;

public class SoulConversionDataAttachmentImpl {

    public static SoulConversionDataAttachment.Data getData(ServerLevel level) {
        SoulConversionDataAttachment.Data data = level.getAttachedOrCreate(TCDataAttachmentsFabric.SOUL_CONVERSION_ATTACHMENT);
        return data;
    }

    public static void setData(ServerLevel level, SoulConversionDataAttachment.Data data) {
        level.setAttached(TCDataAttachmentsFabric.SOUL_CONVERSION_ATTACHMENT, data);
    }
}