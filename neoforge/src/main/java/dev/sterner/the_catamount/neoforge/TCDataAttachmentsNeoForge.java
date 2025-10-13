package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class TCDataAttachmentsNeoForge {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TheCatamount.MOD_ID);

    public static final Supplier<AttachmentType<CatamountPlayerDataAttachment.Data>> CATAMOUNT_ATTACHMENT =
            ATTACHMENT_TYPES.register(
                    "catamount_player_data",
                    () -> AttachmentType.builder(
                                    holder -> new CatamountPlayerDataAttachment.Data()
                            )
                            .serialize(CatamountPlayerDataAttachment.Data.CODEC)
                            .build()
            );
}