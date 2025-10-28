package dev.sterner.the_catamount.client.model;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class CatamountEntityModel extends DefaultedEntityGeoModel<CatamountEntity> {

    public CatamountEntityModel() {
        super(TheCatamount.id("catamount"));
    }

    @Override
    public ResourceLocation getModelResource(CatamountEntity entity) {
        return switch (entity.getStage()) {
            case 3 -> TheCatamount.id("geo/entity/catamount_stage_1.geo.json");
            case 4 -> TheCatamount.id("geo/entity/catamount_stage_2.geo.json");
            case 5 -> TheCatamount.id("geo/entity/catamount_stage_3.geo.json");
            default -> TheCatamount.id("geo/entity/catamount_stage_1.geo.json");
        };
    }

    @Override
    public ResourceLocation getTextureResource(CatamountEntity entity) {
        return switch (entity.getStage()) {
            case 3 -> TheCatamount.id("textures/entity/catamount_stage_1.png");
            case 4 -> TheCatamount.id("textures/entity/catamount_stage_2.png");
            case 5 -> TheCatamount.id("textures/entity/catamount_stage_3.png");
            default -> TheCatamount.id("textures/entity/catamount_stage_1.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(CatamountEntity entity) {
        return switch (entity.getStage()) {
            case 3 -> TheCatamount.id("animations/entity/catamount_stage_1.animation.json");
            case 4 -> TheCatamount.id("animations/entity/catamount_stage_2.animation.json");
            case 5 -> TheCatamount.id("animations/entity/catamount_stage_3.animation.json");
            default -> TheCatamount.id("animations/entity/catamount_stage_1.animation.json");
        };
    }
}