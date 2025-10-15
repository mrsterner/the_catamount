package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.client.ClientPaleAnimalTracker;
import dev.sterner.the_catamount.registry.TCRenderTypes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "getRenderType",
            at = @At("RETURN"),
            cancellable = true
    )
    private void the_catamount$useDesaturatedRenderType(T entity, boolean bodyVisible, boolean translucent,
                                                        boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (entity instanceof Animal && ClientPaleAnimalTracker.isPale(entity.getUUID())) {
            ResourceLocation texture = this.getTextureLocation(entity);
            cir.setReturnValue(TCRenderTypes.entityDesaturated(texture));
        }
    }
}