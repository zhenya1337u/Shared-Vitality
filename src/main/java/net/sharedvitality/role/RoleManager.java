package net.sharedvitality.role;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.sharedvitality.SharedVitalityMod;

import java.util.*;

public class RoleManager {
    private static final Map<UUID, Role> PLAYER_ROLES = new HashMap<>();

    private static final Identifier GUARDIAN_HEALTH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "guardian_health");
    private static final Identifier GUARDIAN_KB_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "guardian_kb");
    private static final Identifier SCOUT_SPEED_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "scout_speed");
    private static final Identifier SCOUT_HEALTH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "scout_health");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static Role getRole(UUID uuid) {
        return PLAYER_ROLES.getOrDefault(uuid, Role.NONE);
    }

    public static void setRole(UUID uuid, Role role) {
        PLAYER_ROLES.put(uuid, role);
    }

    public static Map<UUID, Role> getAllRoles() {
        return Collections.unmodifiableMap(PLAYER_ROLES);
    }

    public static void applyRoleModifiers(ServerPlayer player) {
        Role role = getRole(player.getUUID());

        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(GUARDIAN_HEALTH_ID);
            healthAttr.removeModifier(SCOUT_HEALTH_ID);

            if (role == Role.GUARDIAN) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        GUARDIAN_HEALTH_ID, 10.0, AttributeModifier.Operation.ADD_VALUE));
            } else if (role == Role.SCOUT) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        SCOUT_HEALTH_ID, -4.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance kbAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.removeModifier(GUARDIAN_KB_ID);
            if (role == Role.GUARDIAN) {
                kbAttr.addTransientModifier(new AttributeModifier(
                        GUARDIAN_KB_ID, 0.5, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SCOUT_SPEED_ID);
            if (role == Role.SCOUT) {
                speedAttr.addTransientModifier(new AttributeModifier(
                        SCOUT_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    public static void tickRole(ServerPlayer player) {
        Role role = getRole(player.getUUID());

        if (role == Role.DELVER) {
            // Haste underground
            if (player.getY() < 50) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, false, false));
            } else {
                long clockTime = player.level().getOverworldClockTime() % 24000;
                boolean isDayTime = clockTime < 12000;
                if (isDayTime && player.level().canSeeSky(player.blockPosition())) {
                    // Weakness under open sky during day
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, false));
                }
            }
        }

        if (role == Role.GUARDIAN) {
            // Hunger drains faster
            if (player.level().getServer().getTickCount() % 80 == 0 && player.getFoodData().getFoodLevel() > 0) {
                player.getFoodData().addExhaustion(0.3f);
            }
        }

        if (role == Role.SCOUT) {
            // Restrict diamond/netherite armor
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(armor.getItem());
                    String path = itemId.getPath().toLowerCase();
                    if (path.contains("diamond") || path.contains("netherite")) {
                        player.drop(armor.copy(), true, false);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                        player.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("§cСледопыт не может носить алмазную или незеритовую броню!"), true);
                    }
                }
            }
        }
    }
}
