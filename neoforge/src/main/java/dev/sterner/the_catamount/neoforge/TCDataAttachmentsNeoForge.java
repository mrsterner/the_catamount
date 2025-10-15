package dev.sterner.the_catamount.neoforge;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.data_attachment.*;
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

    public static final Supplier<AttachmentType<SoulConversionDataAttachment.Data>> SOUL_CONVERSION_ATTACHMENT =
            ATTACHMENT_TYPES.register(
                    "soul_conversion_data",
                    () -> AttachmentType.builder(
                                    holder -> new SoulConversionDataAttachment.Data()
                            )
                            .serialize(SoulConversionDataAttachment.Data.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<DangerousLeavesDataAttachment.Data>> DANGEROUS_LEAVES_ATTACHMENT =
            ATTACHMENT_TYPES.register(
                    "dangerous_leaves_data",
                    () -> AttachmentType.builder(
                                    holder -> new DangerousLeavesDataAttachment.Data()
                            )
                            .serialize(DangerousLeavesDataAttachment.Data.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<PaleAnimalDataAttachment.Data>> PALE_ANIMAL_ATTACHMENT =
            ATTACHMENT_TYPES.register(
                    "pale_animal_data",
                    () -> AttachmentType.builder(
                                    holder -> new PaleAnimalDataAttachment.Data()
                            )
                            .serialize(PaleAnimalDataAttachment.Data.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<FrozenAnimalDataAttachment.Data>> FROZEN_ANIMAL_ATTACHMENT =
            ATTACHMENT_TYPES.register(
                    "frozen_animal_data",
                    () -> AttachmentType.builder(
                                    holder -> new FrozenAnimalDataAttachment.Data()
                            )
                            .serialize(FrozenAnimalDataAttachment.Data.CODEC)
                            .build()
            );
}