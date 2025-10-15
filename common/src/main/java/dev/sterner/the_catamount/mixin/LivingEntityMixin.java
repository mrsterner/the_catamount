package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "travel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void the_catamount$preventFrozenTravel(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof Animal && !entity.level().isClientSide) {
            ServerLevel level = (ServerLevel) entity.level();
            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);

            if (data.frozenAnimals().containsKey(entity.getUUID())) {
                entity.setDeltaMovement(Vec3.ZERO);
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "aiStep",
            at = @At("HEAD")
    )
    private void the_catamount$keepFrozenInPlace(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof Animal && !entity.level().isClientSide) {
            ServerLevel level = (ServerLevel) entity.level();
            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);

            FrozenAnimalDataAttachment.FrozenRecord record = data.frozenAnimals().get(entity.getUUID());
            if (record != null) {
                entity.setDeltaMovement(Vec3.ZERO);

                if (entity instanceof Mob mob) {
                    mob.getNavigation().stop();

                    mob.setYBodyRot(record.yaw());
                    mob.setYRot(record.yaw());
                    mob.setYHeadRot(record.yaw());
                    mob.setXRot(record.pitch());
                    mob.yBodyRotO = record.yaw();
                    mob.yRotO = record.yaw();
                    mob.yHeadRotO = record.yaw();
                    mob.xRotO = record.pitch();
                }
            }
        }
    }

    @Inject(
            method = "tickHeadTurn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void the_catamount$preventFrozenHeadTurn(float yRot, float animStep, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof Animal && !entity.level().isClientSide) {
            ServerLevel level = (ServerLevel) entity.level();
            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);

            if (data.frozenAnimals().containsKey(entity.getUUID())) {
                cir.cancel();
            }
        }
    }
}