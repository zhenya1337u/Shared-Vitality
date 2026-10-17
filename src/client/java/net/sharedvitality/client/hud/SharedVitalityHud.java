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

    public static void updateData(float hp, float max, String role, int warning) {
        currentHp = hp;
        maxHp = max;
        currentRole = role;
        warningLevel = warning;
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

        // Bloodlink HUD panel (top-left)
        int x = 10;
        int y = 10;
        int panelWidth = 140;
        int panelHeight = 36;

        // Background panel
        extractor.fill(x - 2, y - 2, x + panelWidth + 2, y + panelHeight + 2, 0x90000000);
        extractor.fill(x - 2, y - 2, x + panelWidth + 2, y, 0xFF8B0000); // Dark red top border

        // Texts
        String hpText = String.format("Общий пул: %.1f / %.1f HP", currentHp, maxHp);
        String roleText = "Роль: " + currentRole;

        extractor.text(font, hpText, x + 4, y + 4, 0xFF5555);
        extractor.text(font, roleText, x + 4, y + 14, 0xFFFF55);

        // Health bar
        int barX = x + 4;
        int barY = y + 24;
        int barWidth = panelWidth - 8;
        int barHeight = 6;

        float ratio = maxHp > 0 ? Math.min(1.0f, Math.max(0.0f, currentHp / maxHp)) : 0.0f;
        int fillWidth = (int) (barWidth * ratio);

        extractor.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333); // Bar background
        int barColor = (ratio > 0.5f) ? 0xFF00DD44 : (ratio > 0.25f ? 0xFFFFAA00 : 0xFFFF2222);
        extractor.fill(barX, barY, barX + fillWidth, barY + barHeight, barColor);
    }
}
