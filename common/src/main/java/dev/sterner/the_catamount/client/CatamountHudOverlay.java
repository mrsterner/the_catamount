package dev.sterner.the_catamount.client;

import dev.sterner.the_catamount.ClientCatamountConfig;
import dev.sterner.the_catamount.data_attachment.CatamountPlayerDataAttachment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CatamountHudOverlay {

    public static void render(GuiGraphics graphics, DeltaTracker partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!ClientCatamountConfig.isHudEnabled()) return;

        CatamountPlayerDataAttachment.Data data = CatamountPlayerDataAttachment.getData(mc.player);

        if (data.catamountStage() < 0) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int x = screenWidth - 120;
        int y = 10;

        int baseHeight = 60;
        int eventHeight = ClientCatamountConfig.getRecentEvents().size() * 12;
        int totalHeight = baseHeight + eventHeight;

        graphics.fill(x - 5, y - 5, x + 115, y + totalHeight, 0x80000000);

        graphics.drawString(mc.font, "Stage: " + data.catamountStage(),
                x, y, 0xFFFFFF);

        graphics.drawString(mc.font, "Points: " + data.points(),
                x, y + 12, 0xFFAA00);

        int nextStagePoints = getNextStageRequirement(data.catamountStage());
        if (nextStagePoints > 0) {
            graphics.drawString(mc.font, "Next: " + nextStagePoints,
                    x, y + 24, 0x888888);
        }

        int yOffset = 36;
        if (data.getEventCooldown() > 0) {
            String cooldown = formatTime(data.getEventCooldown());
            graphics.drawString(mc.font, "Event: " + cooldown,
                    x, y + yOffset, 0xFF5555);
            yOffset += 12;
        } else if (data.getEventCooldown() == 0 && data.catamountStage() >= 0) {
            graphics.drawString(mc.font, "Event: READY",
                    x, y + yOffset, 0x55FF55);
            yOffset += 12;
        }

        if (data.getExtraDamageTimer() > 0) {
            String timer = formatTime(data.getExtraDamageTimer());
            graphics.drawString(mc.font, "Extra Dmg: " + timer,
                    x, y + yOffset, 0xFF0000);
            yOffset += 12;
        }

        if (data.deathCooldownTimer() > 0) {
            String cooldown = formatTime(data.deathCooldownTimer());
            graphics.drawString(mc.font, "Death: " + cooldown,
                    x, y + yOffset, 0x555555);
            yOffset += 12;
        }

        if (!ClientCatamountConfig.getRecentEvents().isEmpty()) {
            yOffset += 6;
            graphics.drawString(mc.font, "Recent Events:",
                    x, y + yOffset, 0xFFFFFF);
            yOffset += 12;

            for (String event : ClientCatamountConfig.getRecentEvents()) {
                int timer = ClientCatamountConfig.getEventTimer(event);
                float alpha = Math.min(1.0f, timer / 20.0f);
                int alphaInt = (int) (alpha * 255);
                int color = (alphaInt << 24) | 0x00FFAA;

                graphics.drawString(mc.font, "• " + event,
                        x + 2, y + yOffset, color);
                yOffset += 12;
            }
        }
    }

    private static int getNextStageRequirement(int currentStage) {
        return switch (currentStage) {
            case 0 -> 0;
            case 1 -> 20;
            case 2 -> 50;
            case 3 -> 100;
            case 4 -> 200;
            default -> -1;
        };
    }

    private static String formatTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return seconds + "s";
    }
}