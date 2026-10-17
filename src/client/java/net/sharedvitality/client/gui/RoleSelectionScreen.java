package net.sharedvitality.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.sharedvitality.network.SelectRolePayload;
import net.sharedvitality.role.Role;

public class RoleSelectionScreen extends Screen {
    public RoleSelectionScreen() {
        super(Component.literal("Выбор роли Кровной связи"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int gap = 24;

        Role[] roles = {
                Role.GUARDIAN, Role.APOTHECARY, Role.DELVER, Role.SCOUT,
                Role.REAPER, Role.ENCHANTER, Role.MASON, Role.BEASTMASTER, Role.SHADOW, Role.SOULBINDER
        };
        for (int i = 0; i < roles.length; i++) {
            Role role = roles[i];
            int y = startY + i * gap;
            this.addRenderableWidget(Button.builder(Component.literal(role.getDisplayName()), btn -> {
                ClientPlayNetworking.send(new SelectRolePayload(role.name()));
                this.onClose();
            }).bounds(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Закрыть"), btn -> this.onClose())
                .bounds(centerX - buttonWidth / 2, startY + roles.length * gap + 10, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);
        extractor.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 25, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
