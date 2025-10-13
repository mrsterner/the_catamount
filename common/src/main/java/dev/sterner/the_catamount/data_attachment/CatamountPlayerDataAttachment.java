package dev.sterner.the_catamount.data_attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.sterner.the_catamount.TheCatamount;
import dev.sterner.the_catamount.entity.CatamountEntity;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class CatamountPlayerDataAttachment {

    @ExpectPlatform
    public static Data getData(Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setData(Player player, Data data) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sync(Player player, Data data) {
        throw new AssertionError();
    }

    public static void tellDeath(ServerPlayer player, CatamountEntity catamountEntity) {
        Data data = getData(player);
        Data newData = data
                .withCatamountUUID(Optional.of(catamountEntity.getUUID()))
                .withDeathCooldownTimer(7 * 24 * 60 * 20);
        setData(player, newData);
    }

    public static void tick(ServerPlayer player) {
        Data data = getData(player);
        boolean updated = false;

        if (data.deathCooldownTimer() > 0) {
            data = data.withDeathCooldownTimer(data.deathCooldownTimer() - 1);
            updated = true;
        }

        if (data.catamountStage() == 0 || data.catamountStage() == 1) {
            data = data.addStageTimer(1);
            updated = true;
        }

        if (updated) {
            setData(player, data);
        }
    }

    public static boolean tryAdvanceStage(Player player) {
        Data data = getData(player);
        int currentStage = data.catamountStage();
        int points = data.points();
        int stageTimer = data.stageTimer();

        int nextStage = -1;

        switch (currentStage) {
            case 0 -> {
                if (points >= 0 || stageTimer >= 7 * 24 * 60 * 20) {
                    nextStage = 1;
                }
            }
            case 1 -> {
                if (points >= 20 || stageTimer >= 5 * 24 * 60 * 20) {
                    nextStage = 2;
                }
            }
            case 2 -> { if (points >= 50) nextStage = 3; }
            case 3 -> { if (points >= 100) nextStage = 4; }
            case 4 -> { if (points >= 200) nextStage = 5; }
            default -> {}
        }

        if (nextStage > 0) {
            Data newData = data.withCatamountStage(nextStage).withStageTimer(0);
            setData(player, newData);
            return true;
        }

        return false;
    }


    public record Data(
            Optional<UUID> catamountUUID,
            int catamountStage,
            int points,
            int deathCooldownTimer,
            int stageTimer,
            int eventCooldown,
            int extraDamageTimer,
            int soulFireTimer,
            int feedingFrenzyCooldown
    ) {

        public Data() {
            this(Optional.empty(), -1, 0, 0, 0, 0, 0, 0, 0);
        }

        public Data withStageTimer(int timer) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, timer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withCatamountStage(int stage) {
            return new Data(catamountUUID, stage, points, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data addStageTimer(int delta) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, stageTimer + delta, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data addPoints(int amount) {
            return new Data(catamountUUID, catamountStage, points + amount, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withCatamountUUID(Optional<UUID> newUUID) {
            return new Data(newUUID, catamountStage, points, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withPoints(int newPoints) {
            return new Data(catamountUUID, catamountStage, newPoints, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withDeathCooldownTimer(int newCooldown) {
            return new Data(catamountUUID, catamountStage, points, newCooldown, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withEventCooldown(int cooldown) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, stageTimer, cooldown, extraDamageTimer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withExtraDamageTimer(int timer) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, stageTimer, eventCooldown, timer, soulFireTimer, feedingFrenzyCooldown);
        }

        public Data withSoulFireTimer(int timer) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, timer, feedingFrenzyCooldown);
        }

        public Data withFeedingFrenzyCooldown(int cooldown) {
            return new Data(catamountUUID, catamountStage, points, deathCooldownTimer, stageTimer, eventCooldown, extraDamageTimer, soulFireTimer, cooldown);
        }

        public int getFeedingFrenzyCooldown() {
            return feedingFrenzyCooldown;
        }

        public int getEventCooldown() {
            return eventCooldown;
        }

        public int getExtraDamageTimer() {
            return extraDamageTimer;
        }

        public int getSoulFireTimer() {
            return soulFireTimer;
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.optionalFieldOf("catamountUUID").forGetter(c -> c.catamountUUID),
                Codec.INT.fieldOf("catamountStage").forGetter(d -> d.catamountStage),
                Codec.INT.fieldOf("duration").forGetter(d -> d.points),
                Codec.INT.fieldOf("deathCooldownTimer").forGetter(d -> d.deathCooldownTimer),
                Codec.INT.fieldOf("stageTimer").forGetter(d -> d.stageTimer),
                Codec.INT.optionalFieldOf("eventCooldown", 0).forGetter(d -> d.eventCooldown),
                Codec.INT.optionalFieldOf("extraDamageTimer", 0).forGetter(d -> d.extraDamageTimer),
                Codec.INT.optionalFieldOf("soulFireTimer", 0).forGetter(d -> d.soulFireTimer),
                Codec.INT.optionalFieldOf("feedingFrenzyCooldown", 0).forGetter(d -> d.feedingFrenzyCooldown)
        ).apply(instance, Data::new));

        public static final ResourceLocation ID = TheCatamount.id("catamount_player_data");
    }
}
