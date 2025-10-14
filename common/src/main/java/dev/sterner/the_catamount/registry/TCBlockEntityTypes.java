package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.block.MonstrousHeadBlockEntity;
import dev.sterner.the_catamount.block.SuspiciousDirtBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TCBlockEntityTypes {

    public static BlockEntityType<SuspiciousDirtBlockEntity> SUSPICIOUS_DIRT = BlockEntityType.Builder
                .of(SuspiciousDirtBlockEntity::new,
                        TCBlocks.SUSPICIOUS_DIRT)
                .build(null);

    public static BlockEntityType<MonstrousHeadBlockEntity> MONSTROUS_REMAINS = BlockEntityType.Builder
            .of(MonstrousHeadBlockEntity::new,
                    TCBlocks.MONSTROUS_SKULL)
            .build(null);
}