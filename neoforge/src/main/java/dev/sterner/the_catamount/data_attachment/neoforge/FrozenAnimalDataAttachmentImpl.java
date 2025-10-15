package dev.sterner.the_catamount.data_attachment.neoforge;

import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import dev.sterner.the_catamount.neoforge.TCDataAttachmentsNeoForge;
import net.minecraft.server.level.ServerLevel;

public class FrozenAnimalDataAttachmentImpl {

    public static FrozenAnimalDataAttachment.Data getData(ServerLevel level) {
        return level.getData(TCDataAttachmentsNeoForge.FROZEN_ANIMAL_ATTACHMENT);
    }

    public static void setData(ServerLevel level, FrozenAnimalDataAttachment.Data data) {
        level.setData(TCDataAttachmentsNeoForge.FROZEN_ANIMAL_ATTACHMENT, data);
    }
}
