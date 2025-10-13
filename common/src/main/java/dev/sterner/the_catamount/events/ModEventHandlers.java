package dev.sterner.the_catamount.events;

import dev.sterner.the_catamount.catamount_events.CatamountEventHandler;
import dev.sterner.the_catamount.catamount_events.DamagingEvents;
import dev.sterner.the_catamount.catamount_events.PassiveEvents;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ModEventHandlers {

    public static void onServerTickPlayer(ServerPlayer player) {
        CatamountPlayerDataAttachment.tick(player);

        CatamountEventHandler.tick(player);

        DamagingEvents.DangerousLeavesEvent.tickDangerousLeaves(player);
    }

    public static void onServerLevelTick(ServerLevel level) {
        PassiveEvents.SoulFireConversionEvent.tickConversions(level);
    }

    public static float onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
        if (entity instanceof ServerPlayer player) {
            amount = CatamountEventHandler.onPlayerDamage(player, source, amount);
        }

        return amount;
    }

    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (source.getEntity() instanceof CatamountEntity catamount) {
            ServerPlayer nearestPlayer = findNearestPlayer(entity);
            if (nearestPlayer != null) {
                int points = calculatePointsForKill(entity, catamount.isFeedingFrenzy());
                CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(nearestPlayer);
                CatamountPlayerDataAttachment.setData(nearestPlayer, data.addPoints(points));
            }
        }
    }

    private static ServerPlayer findNearestPlayer(LivingEntity entity) {
        return entity.level().getNearestPlayer(entity, 64.0) instanceof ServerPlayer sp ? sp : null;
    }

    private static int calculatePointsForKill(LivingEntity entity, boolean feedingFrenzy) {
        float maxHealth = entity.getMaxHealth();

        if (feedingFrenzy) {
            return (int) maxHealth;
        } else {
            return (int) (maxHealth / 2);
        }
    }
}