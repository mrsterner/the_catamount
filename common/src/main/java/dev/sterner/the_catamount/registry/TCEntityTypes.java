package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class TCEntityTypes {

    public static final EntityType<CatamountEntity> CATAMOUNT = EntityType.Builder.of(
            CatamountEntity::new, MobCategory.MONSTER)
            .sized(2.0F, 4.5F)
            .eyeHeight(4.35F)
            .clientTrackingRange(8)
            .build("catamount");

    public static final EntityType<DevouredEntity> DEVOURED = EntityType.Builder.of(DevouredEntity::new, MobCategory.MONSTER).sized(0.25F, 0.25F).eyeHeight(0.25F).clientTrackingRange(8).build("devoured");
    public static final EntityType<WindEntity> WIND = EntityType.Builder.of(WindEntity::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(8).build("wind");
    public static final EntityType<LightOrbEntity> LIGHT_ORB = EntityType.Builder.of(LightOrbEntity::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(8).build("light_orb");
}
