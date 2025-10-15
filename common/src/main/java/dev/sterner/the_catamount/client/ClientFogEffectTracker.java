package dev.sterner.the_catamount.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;


public class ClientFogEffectTracker {
    private static long fogEndTime = 0;
    private static long fogStartTime = 0;
    private static Vec3 fogCenter = Vec3.ZERO;
    private static float fogRadius = 64.0f;
    private static final int FADE_IN_TIME = 100;
    private static final int FADE_OUT_TIME = 100;

    public static void setFogEffect(Vec3 center, float radius, int durationTicks) {
        if (Minecraft.getInstance().level == null) return;

        fogCenter = center;
        fogRadius = radius;
        fogStartTime = Minecraft.getInstance().level.getGameTime();
        fogEndTime = fogStartTime + durationTicks;
    }

    public static void clearFog() {
        fogEndTime = 0;
        fogStartTime = 0;
    }

    public static boolean hasFogEffect() {
        if (Minecraft.getInstance().level == null) return false;
        return Minecraft.getInstance().level.getGameTime() < fogEndTime;
    }

    public static float getFogTransition() {
        if (!hasFogEffect() || Minecraft.getInstance().level == null) return 0.0f;

        long currentTime = Minecraft.getInstance().level.getGameTime();
        long elapsed = currentTime - fogStartTime;
        long remaining = fogEndTime - currentTime;

        if (elapsed < FADE_IN_TIME) {
            return (float) elapsed / FADE_IN_TIME;
        }

        if (remaining < FADE_OUT_TIME) {
            return (float) remaining / FADE_OUT_TIME;
        }

        return 1.0f;
    }

    public static float getFogDensity(Vec3 playerPos) {
        if (!hasFogEffect()) return 0.0f;

        double distance = playerPos.distanceTo(fogCenter);
        if (distance > fogRadius) return 0.0f;

        float transition = getFogTransition();
        float density = 1.0f - (float) (distance / fogRadius);
        return Math.min(1.0f, density * 0.8f * transition);
    }

    public static float getSkyDarkness(Vec3 playerPos) {
        if (!hasFogEffect()) return 0.0f;

        double distance = playerPos.distanceTo(fogCenter);
        if (distance > fogRadius) return 0.0f;

        float transition = getFogTransition();
        float darkness = 1.0f - (float) (distance / fogRadius);
        return Math.min(0.85f, darkness * transition);
    }

    public static void tick() {
        if (Minecraft.getInstance().level == null) return;

        long currentTime = Minecraft.getInstance().level.getGameTime();
        if (currentTime >= fogEndTime) {
            fogEndTime = 0;
            fogStartTime = 0;
        }
    }
}