package dev.sterner.the_catamount.fabric;

import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class TCDataAttachmentsFabric {


    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<CatamountPlayerDataAttachment.Data> CATAMOUNT_ATTACHMENT =
            AttachmentRegistry.<CatamountPlayerDataAttachment.Data>builder()
                    .persistent(CatamountPlayerDataAttachment.Data.CODEC)
                    .initializer(CatamountPlayerDataAttachment.Data::new)
                    .buildAndRegister(CatamountPlayerDataAttachment.Data.ID);
}
