package dev.sterner.the_catamount.client.model;

import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class CatamountEntityModel extends DefaultedEntityGeoModel<CatamountEntity> {
    public CatamountEntityModel() {
        super(TheCatamount.id("catamount"));
    }
}
