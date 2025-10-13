package dev.sterner.the_catamount.registry;

import dev.sterner.the_catamount.TheCatamount;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class TCTags {

    public TagKey<EntityType<?>> VALUABLE_MOBS = TagKey.create(Registries.ENTITY_TYPE, TheCatamount.id("valuable_mobs"));
    public TagKey<EntityType<?>> IGNORED_MOBS = TagKey.create(Registries.ENTITY_TYPE, TheCatamount.id("ignored_mobs"));
}
