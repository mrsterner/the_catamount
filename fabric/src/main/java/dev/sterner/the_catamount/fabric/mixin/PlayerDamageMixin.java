package dev.sterner.the_catamount.fabric.mixin;


import com.mojang.authlib.GameProfile;
import dev.sterner.the_catamount.events.ModEventHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerDamageMixin extends LivingEntity {

    protected PlayerDamageMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(
            method = "hurt",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float modifyDamageAmount(float amount, DamageSource source) {
        if ((Object) this instanceof ServerPlayer player) {
            return ModEventHandlers.onLivingDamage(player, source, amount);
        }
        return amount;
    }
}