package dev.sterner.the_catamount.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class CatamountEntity extends Mob {
    public CatamountEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
}
