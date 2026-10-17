package net.sharedvitality.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SharedVitalityHud implements HudElement {
    private static float currentHp = 20.0f;
    private static float maxHp = 20.0f;
    private static String currentRole = "Нет роли";
    private static int warningLevel = 0;
    private static boolean hasChosenRole = false;
    private static int readyCount = 0;
    private static int totalPlayers = 1;
    private static String takenRolesJson = "";

    public static void updateData(float hp, float max, String role, int warning, boolean chosen, int ready, int total, String taken) {
        currentHp = hp;
        maxHp = max;
        currentRole = role;
        warningLevel = warning;
        hasChosenRole = chosen;
        readyCount = ready;
        totalPlayers = total;
        takenRolesJson = taken;
    }

    public static boolean hasChosenRole() {
        return hasChosenRole;
    }

    public static int getReadyCount() {
        return readyCount;
    }

    public static int getTotalPlayers() {
        return totalPlayers;
    }

    public static String getTakenRolesJson() {
        return takenRolesJson;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) return;

        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Warning vignette overlay when tether is stretched or severed
        if (warningLevel == 1) {
            // Slight amber tint
            extractor.fill(0, 0, screenWidth, 4, 0xAAFFA500);
            extractor.fill(0, screenHeight - 4, screenWidth, screenHeight, 0xAAFFA500);
        } else if (warningLevel == 2) {
            // Red border warning
            extractor.fill(0, 0, screenWidth, 6, 0xCCFF0000);
            extractor.fill(0, screenHeight - 6, screenWidth, screenHeight, 0xCCFF0000);
            extractor.fill(0, 0, 6, screenHeight, 0xCCFF0000);
            extractor.fill(screenWidth - 6, 0, screenWidth, screenHeight, 0xCCFF0000);
        }

        // Action Bar area HUD (centered above hotbar)
        // In vanilla Minecraft, hotbar is at screenHeight - 22, health/hunger icons at screenHeight - 39
        // Action bar messages are at screenHeight - 68
        int currentHearts = (int) Math.ceil(currentHp / 2.0f);
        int maxHearts = (int) Math.ceil(maxHp / 2.0f);

        String hudText = String.format("❤ ПУЛ: %d / %d сердец  •  %s", currentHearts, maxHearts, currentRole);
        int textWidth = font.width(hudText);

        int barWidth = Math.max(textWidth + 16, 120);
        int barHeight = 16;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 65; // Just above the vanilla health/hunger/air bars

        // Sleek translucent background with dark red border
        extractor.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0x77000000);
        extractor.fill(x - 2, y - 2, x + barWidth + 2, y - 1, 0xFF8B0000); // Crimson top accent

        // Mini HP progress indicator bar at the bottom of the pill
        float ratio = maxHp > 0 ? Math.min(1.0f, Math.max(0.0f, currentHp / maxHp)) : 0.0f;
        int miniBarWidth = barWidth;
        int fillWidth = (int) (miniBarWidth * ratio);
        int barColor = (ratio > 0.5f) ? 0xFF00DD44 : (ratio > 0.25f ? 0xFFFFAA00 : 0xFFFF2222);

        extractor.fill(x, y + barHeight - 2, x + miniBarWidth, y + barHeight, 0xFF222222);
        extractor.fill(x, y + barHeight - 2, x + fillWidth, y + barHeight, barColor);

        // Centered text
        int textX = (screenWidth - textWidth) / 2;
        int textY = y + 2;
        extractor.text(font, hudText, textX, textY, 0xFFFFFF);
    }
}
