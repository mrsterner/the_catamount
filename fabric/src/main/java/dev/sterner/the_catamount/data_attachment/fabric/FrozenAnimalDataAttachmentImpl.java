package dev.sterner.the_catamount.data_attachment.fabric;

import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import dev.sterner.the_catamount.data_attachment.TCDataAttachmentsFabric;
import net.minecraft.server.level.ServerLevel;

public class FrozenAnimalDataAttachmentImpl {

    public static FrozenAnimalDataAttachment.Data getData(ServerLevel level) {
        return level.getAttachedOrCreate(TCDataAttachmentsFabric.FROZEN_ANIMAL_ATTACHMENT);
    }

    public static void setData(ServerLevel level, FrozenAnimalDataAttachment.Data data) {
        level.setAttached(TCDataAttachmentsFabric.FROZEN_ANIMAL_ATTACHMENT, data);
    }
}