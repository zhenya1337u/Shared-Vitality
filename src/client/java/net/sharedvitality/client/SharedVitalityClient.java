package net.sharedvitality.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.sharedvitality.SharedVitalityMod;
import net.sharedvitality.client.gui.OwoRoleSelectionScreen;
import net.sharedvitality.client.hud.SharedVitalityHud;
import net.sharedvitality.network.SyncPoolPayload;
import org.lwjgl.glfw.GLFW;

public class SharedVitalityClient implements ClientModInitializer {
    private static KeyMapping openRoleMenuKey;

    @Override
    public void onInitializeClient() {
        // Register HUD element
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "bloodlink_hud"),
                new SharedVitalityHud()
        );

        // Register keybinding to open role selection screen (default: 'R')
        openRoleMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.sharedvitality.open_role_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openRoleMenuKey.consumeClick()) {
                client.setScreenAndShow(new OwoRoleSelectionScreen());
            }
        });

        // Register receiver for SyncPoolPayload
        ClientPlayNetworking.registerGlobalReceiver(SyncPoolPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                SharedVitalityHud.updateData(
                        payload.currentHp(),
                        payload.maxHp(),
                        payload.roleName(),
                        payload.warningLevel()
                );
            });
        });
    }
}
