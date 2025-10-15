package dev.sterner.the_catamount.mixin;

import dev.sterner.the_catamount.data_attachment.FrozenAnimalDataAttachment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Mob.class)
public abstract class MobMixin {

    @Inject(
            method = "serverAiStep",
            at = @At("HEAD"),
            cancellable = true
    )
    private void the_catamount$preventFrozenAI(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        if (mob instanceof Animal && !mob.level().isClientSide) {
            ServerLevel level = (ServerLevel) mob.level();
            FrozenAnimalDataAttachment.Data data = FrozenAnimalDataAttachment.getData(level);

            FrozenAnimalDataAttachment.FrozenRecord record = data.frozenAnimals().get(mob.getUUID());
            if (record != null) {
                long currentTime = level.getGameTime();

                if (currentTime < record.endTime()) {
                    mob.getLookControl().setLookAt(mob.getX(), mob.getY(), mob.getZ());
                    ci.cancel();
                }
            }
        }
    }
}