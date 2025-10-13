package dev.sterner.the_catamount.data_attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class TCDataAttachmentsFabric {


    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<CatamountPlayerDataAttachment.Data> CATAMOUNT_ATTACHMENT =
            AttachmentRegistry.<CatamountPlayerDataAttachment.Data>builder()
                    .persistent(CatamountPlayerDataAttachment.Data.CODEC)
                    .initializer(CatamountPlayerDataAttachment.Data::new)
                    .buildAndRegister(CatamountPlayerDataAttachment.Data.ID);

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<SoulConversionDataAttachment.Data> SOUL_CONVERSION_ATTACHMENT =
            AttachmentRegistry.<SoulConversionDataAttachment.Data>builder()
                    .persistent(SoulConversionDataAttachment.Data.CODEC)
                    .initializer(SoulConversionDataAttachment.Data::new)
                    .buildAndRegister(SoulConversionDataAttachment.Data.ID);

    public static void init() {

    }
}
