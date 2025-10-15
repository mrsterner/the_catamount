package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.entity.DevouredEntity;
import dev.sterner.the_catamount.entity.LightOrbEntity;
import dev.sterner.the_catamount.entity.WindEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class TCEntityTypes {

    public static final EntityType<CatamountEntity> CATAMOUNT = EntityType.Builder.of(CatamountEntity::new, MobCategory.MONSTER).sized(0.5F, 1.5F).eyeHeight(1.35F).clientTrackingRange(8).build("catamount");


    public static final EntityType<DevouredEntity> DEVOURED = EntityType.Builder.of(DevouredEntity::new, MobCategory.MONSTER).sized(0.25F, 0.25F).eyeHeight(0.25F).clientTrackingRange(8).build("devoured");
    public static final EntityType<WindEntity> WIND = EntityType.Builder.of(WindEntity::new, MobCategory.AMBIENT).sized(0.1F, 0.1F).clientTrackingRange(8).build("wind");
    public static final EntityType<LightOrbEntity> LIGHT_ORB = EntityType.Builder.of(LightOrbEntity::new, MobCategory.AMBIENT).sized(0.1F, 0.1F).clientTrackingRange(8).build("light_orb");
}
