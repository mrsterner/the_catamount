package dev.sterner.the_catamount;

import com.mojang.logging.LogUtils;
import dev.sterner.the_catamount.entity.CatamountEntity;
import dev.sterner.the_catamount.registry.TCMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

public class TheCatamount {

    public static final String MOD_ID = "the_catamount";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Biome> HAUNTED = TagKey.create(Registries.BIOME, id("haunted"));

    public static final EntityDataSerializer<CatamountEntity.AttackType> ATTACK_TYPE = EntityDataSerializer.forValueType(CatamountEntity.AttackType.STREAM_CODEC);

    public static void init() {
        TCMobEffects.init();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
