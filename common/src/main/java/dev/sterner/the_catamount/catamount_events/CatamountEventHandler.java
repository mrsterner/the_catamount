package dev.sterner.the_catamount.catamount_events;

import dev.sterner.the_catamount.PlatformHelper;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import dev.sterner.the_catamount.payload.EventTriggeredPayload;
import dev.sterner.the_catamount.registry.TCMobEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CatamountEventHandler {

    private static final int MIN_BATCH_COOLDOWN = 20 * 60 * 3;
    private static final int RETRY_COOLDOWN = 20 * 60;

    public static void tick(ServerPlayer player) {
        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);

        data = tickActiveEvents(player, data);

        if (data.getEventCooldown() > 0) {
            data = data.withEventCooldown(data.getEventCooldown() - 1);
            CatamountPlayerDataAttachment.setData(player, data);
        }

        if (data.getEventCooldown() <= 0) {
            tryTriggerBatch(player, data);
        }
    }

    private static CatamountPlayerDataAttachment.Data tickActiveEvents(ServerPlayer player, CatamountPlayerDataAttachment.Data data) {
        boolean needsUpdate = false;

        if (data.getExtraDamageTimer() > 0) {
            data = data.withExtraDamageTimer(data.getExtraDamageTimer() - 1);
            needsUpdate = true;
        }

        if (data.getSoulFireTimer() > 0) {
            data = data.withSoulFireTimer(data.getSoulFireTimer() - 1);
            needsUpdate = true;
        }

        if (data.getFeedingFrenzyCooldown() > 0) {
            data = data.withFeedingFrenzyCooldown(data.getFeedingFrenzyCooldown() - 1);
            needsUpdate = true;
        }

        if (needsUpdate) {
            CatamountPlayerDataAttachment.setData(player, data);
        }

        return data;
    }

    private static void tryTriggerBatch(ServerPlayer player, CatamountPlayerDataAttachment.Data data) {
        RandomSource random = player.getRandom();

        int coinFlips = getTimeOfDayCoins(player);
        boolean passedCoinFlip = false;

        for (int i = 0; i < coinFlips; i++) {
            if (random.nextBoolean()) {
                passedCoinFlip = true;
                break;
            }
        }

        if (!passedCoinFlip) {
            CatamountPlayerDataAttachment.setData(player, data.withEventCooldown(RETRY_COOLDOWN));
            return;
        }

        double moonBatchChance = getMoonPhaseBatchChance(player.level());
        if (random.nextDouble() > moonBatchChance) {
            CatamountPlayerDataAttachment.setData(player, data.withEventCooldown(RETRY_COOLDOWN));
            return;
        }

        int eventsInBatch = determineBatchSize(player.level(), random);

        executeEventBatch(player, data, eventsInBatch);

        CatamountPlayerDataAttachment.setData(player, data.withEventCooldown(MIN_BATCH_COOLDOWN));
    }

    private static int getTimeOfDayCoins(ServerPlayer player) {
        long dayTime = player.level().getDayTime() % 24000;

        if (dayTime >= 1000 && dayTime < 13000) return 1;

        if (dayTime >= 13000 && dayTime < 23000) return 3;

        return 2;
    }

    private static double getMoonPhaseBatchChance(Level level) {
        int moonPhase = level.getMoonPhase();

        return switch (moonPhase) {
            case 0 -> 0.90;
            case 1, 7 -> 0.75;
            case 2, 6 -> 0.50;
            case 3, 5 -> 0.33;
            case 4 -> 0.20;
            default -> 0.50;
        };
    }

    private static int determineBatchSize(Level level, RandomSource random) {
        int moonPhase = level.getMoonPhase();
        double roll = random.nextDouble();

        return switch (moonPhase) {
            case 0 -> {
                if (roll < 0.05) yield 1;
                else if (roll < 0.34) yield 2;
                else yield 3;
            }
            case 1, 7 -> {
                if (roll < 0.10) yield 1;
                else if (roll < 0.50) yield 2;
                else yield 3;
            }
            case 2, 6 -> {
                if (roll < 0.33) yield 1;
                else if (roll < 0.67) yield 2;
                else yield 3;
            }
            case 3, 5 -> {
                if (roll < 0.66) yield 1;
                else yield 2;
            }
            case 4 -> {
                if (roll < 0.75) yield 1;
                else yield 2;
            }
            default -> 1;
        };
    }

    private static void executeEventBatch(ServerPlayer player, CatamountPlayerDataAttachment.Data data, int count) {
        List<CatamountEvent> validEvents = getValidEventsForPlayer(player, data);

        if (validEvents.isEmpty()) return;

        Collections.shuffle(validEvents, new Random(player.getRandom().nextLong()));

        for (int i = 0; i < Math.min(count, validEvents.size()); i++) {
            CatamountEvent event = validEvents.get(i);
            event.execute(player);

            String eventName = getEventName(event);
            EventTriggeredPayload payload = new EventTriggeredPayload(eventName);
            PlatformHelper.sendPayloadToPlayer(player, payload);
        }
    }

    private static String getEventName(CatamountEvent event) {
        String className = event.getClass().getSimpleName();
        return className.replace("Event", "")
                .replaceAll("([A-Z])", " $1")
                .trim();
    }

    private static List<CatamountEvent> getValidEventsForPlayer(ServerPlayer player, CatamountPlayerDataAttachment.Data data) {
        List<CatamountEvent> events = new ArrayList<>();
        int stage = data.catamountStage();

        events.addAll(PassiveEvents.getAll());

        if (stage >= 1 && isValidBiomeForDamaging(player)) {
            events.addAll(DamagingEvents.getAll());
        }

        if (stage >= 2 && isValidBiomeForDamaging(player)) {
            events.addAll(DangerousEvents.getAll());
        }

        if (stage >= 3 && isValidBiomeForDamaging(player)) {
            events.addAll(DeadlyEvents.getAll());
        }

        return events;
    }

    private static boolean isValidBiomeForDamaging(ServerPlayer player) {
        ResourceKey<Biome> biomeKey = player.level().getBiome(player.blockPosition()).unwrapKey().orElse(null);
        if (biomeKey == null) return false;

        String biomePath = biomeKey.location().getPath();
        return !biomePath.contains("ocean") &&
                !biomePath.contains("cave") &&
                !biomePath.contains("desert") &&
                !biomePath.contains("badlands");
    }

    public static float onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(player);

        if (data.getExtraDamageTimer() > 0) {
            if (isQualifyingDamageSource(source)) {
                float extraDamage = 1.0f + player.getRandom().nextFloat();

                int points = (int) extraDamage;
                CatamountPlayerDataAttachment.setData(player, data.addPoints(points));

                if (player.getRandom().nextFloat() < 0.3f) {
                    applyBleeding(player);
                }

                return amount + extraDamage;
            }
        }

        return amount;
    }

    private static boolean isQualifyingDamageSource(DamageSource source) {
        return source.is(DamageTypes.CACTUS) ||
                source.is(DamageTypes.SWEET_BERRY_BUSH) ||
                source.is(DamageTypes.MOB_ATTACK) ||
                source.is(DamageTypes.FALL);
    }

    private static void applyBleeding(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(TCMobEffects.BLEEDING, 20 * 4, 0));
    }
}