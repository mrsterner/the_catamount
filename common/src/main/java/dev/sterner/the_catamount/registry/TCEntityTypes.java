package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class TCEntityTypes {

    public static final EntityType<CatamountEntity> CATAMOUNT = EntityType.Builder.of(CatamountEntity::new, MobCategory.MONSTER).sized(0.5F, 0.5F).eyeHeight(0.65F).clientTrackingRange(8).build("catamount");
}
