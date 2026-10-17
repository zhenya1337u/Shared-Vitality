package net.sharedvitality.role;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Items;
import net.sharedvitality.SharedVitalityMod;
import net.sharedvitality.pool.HealthPoolManager;

import java.util.*;

public class RoleManager {
    private static final Map<UUID, Role> PLAYER_ROLES = new HashMap<>();
    private static final Set<UUID> CHOSEN_ONCE = new HashSet<>();

    private static final Identifier GUARDIAN_HEALTH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "guardian_health");
    private static final Identifier GUARDIAN_KB_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "guardian_kb");
    private static final Identifier SCOUT_SPEED_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "scout_speed");
    private static final Identifier SCOUT_HEALTH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "scout_health");
    private static final Identifier SHADOW_HEALTH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "shadow_health");
    private static final Identifier ENCHANTER_ARMOR_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "enchanter_armor");
    private static final Identifier MASON_REACH_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "mason_reach");
    private static final Identifier MASON_WATER_ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "mason_water");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static Role getRole(UUID uuid) {
        return PLAYER_ROLES.getOrDefault(uuid, Role.NONE);
    }

    public static void setRole(UUID uuid, Role role) {
        PLAYER_ROLES.put(uuid, role);
    }

    public static boolean hasChosenRole(UUID uuid) {
        return CHOSEN_ONCE.contains(uuid);
    }

    public static void markRoleChosen(UUID uuid) {
        CHOSEN_ONCE.add(uuid);
    }

    public static void resetRoleChosen(UUID uuid) {
        CHOSEN_ONCE.remove(uuid);
    }

    public static Map<UUID, Role> getAllRoles() {
        return Collections.unmodifiableMap(PLAYER_ROLES);
    }

    public static void applyRoleModifiers(ServerPlayer player) {
        Role role = getRole(player.getUUID());

        // MAX HEALTH
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(GUARDIAN_HEALTH_ID);
            healthAttr.removeModifier(SCOUT_HEALTH_ID);
            healthAttr.removeModifier(SHADOW_HEALTH_ID);

            if (role == Role.GUARDIAN) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        GUARDIAN_HEALTH_ID, 10.0, AttributeModifier.Operation.ADD_VALUE));
            } else if (role == Role.SCOUT) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        SCOUT_HEALTH_ID, -4.0, AttributeModifier.Operation.ADD_VALUE));
            } else if (role == Role.SHADOW) {
                healthAttr.addTransientModifier(new AttributeModifier(
                        SHADOW_HEALTH_ID, -6.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // KNOCKBACK RESISTANCE
        AttributeInstance kbAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.removeModifier(GUARDIAN_KB_ID);
            if (role == Role.GUARDIAN) {
                kbAttr.addTransientModifier(new AttributeModifier(
                        GUARDIAN_KB_ID, 0.5, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // MOVEMENT SPEED
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SCOUT_SPEED_ID);
            if (role == Role.SCOUT) {
                speedAttr.addTransientModifier(new AttributeModifier(
                        SCOUT_SPEED_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // ENCHANTER ARMOR DEBUFF (-30%)
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ENCHANTER_ARMOR_ID);
            if (role == Role.ENCHANTER) {
                armorAttr.addTransientModifier(new AttributeModifier(
                        ENCHANTER_ARMOR_ID, -0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // MASON REACH & WATER SPEED
        AttributeInstance reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.removeModifier(MASON_REACH_ID);
            if (role == Role.MASON) {
                reachAttr.addTransientModifier(new AttributeModifier(
                        MASON_REACH_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance waterAttr = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (waterAttr != null) {
            waterAttr.removeModifier(MASON_WATER_ID);
            if (role == Role.MASON) {
                waterAttr.addTransientModifier(new AttributeModifier(
                        MASON_WATER_ID, -0.40, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    public static void tickRole(ServerPlayer player) {
        Role role = getRole(player.getUUID());
        int tickCount = player.level().getServer().getTickCount();

        // 1. REAPER: when pool < 30%, Strength II and Speed I
        if (role == Role.REAPER) {
            float poolRatio = HealthPoolManager.getMaxHp() > 0 ? (HealthPoolManager.getCurrentHp() / HealthPoolManager.getMaxHp()) : 1.0f;
            if (poolRatio < 0.30f) {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
            }
            if (player.isOnFire()) {
                // Burning duration increased by 50%
                int fire = player.getRemainingFireTicks();
                if (fire > 0 && tickCount % 20 == 0) {
                    player.setRemainingFireTicks(fire + 10);
                }
            }
        }

        // 2. ENCHANTER: Book aura (near ally <= 8 blocks buffs allies with element effects)
        if (role == Role.ENCHANTER && tickCount % 40 == 0) {
            for (ServerPlayer ally : player.level().getServer().getPlayerList().getPlayers()) {
                if (ally != player && ally.distanceTo(player) <= 8.0f) {
                    ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, false));
                }
            }
        }

        // 3. MASON: Citadel aura (Resistance I when near crafting table / inside base)
        if (role == Role.MASON) {
            BlockPos pos = player.blockPosition();
            boolean hasCraftingTable = false;
            // Search 16-block radius for crafting table/workbench
            if (tickCount % 40 == 0) {
                Iterable<BlockPos> area = BlockPos.betweenClosed(pos.offset(-12, -4, -12), pos.offset(12, 6, 12));
                for (BlockPos p : area) {
                    if (player.level().getBlockState(p).is(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)) {
                        hasCraftingTable = true;
                        break;
                    }
                }
                if (hasCraftingTable) {
                    for (ServerPlayer ally : player.level().getServer().getPlayerList().getPlayers()) {
                        if (ally.distanceTo(player) <= 24.0f) {
                            ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0, true, false, false));
                        }
                    }
                } else if (player.getFoodData().getFoodLevel() > 0 && tickCount % 60 == 0) {
                    // Out in open wild without base: 20% faster hunger
                    player.getFoodData().addExhaustion(0.3f);
                }
            }
        }

        // 4. BEASTMASTER: Attack slowdown in dark caves without sky access
        if (role == Role.BEASTMASTER) {
            if (!player.level().canSeeSky(player.blockPosition()) && player.getY() < 60) {
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, true, false, false));
            }
        }

        // 5. SHADOW: Sneaking grants invisible/stealth; restrict shields
        if (role == Role.SHADOW) {
            if (player.isCrouching()) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, false));
            }
            // Cannot use shields
            if (player.getMainHandItem().is(Items.SHIELD)) {
                player.drop(player.getMainHandItem().copy(), true, false);
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cТень не может использовать щиты!"), true);
            }
            if (player.getOffhandItem().is(Items.SHIELD)) {
                player.drop(player.getOffhandItem().copy(), true, false);
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cТень не может использовать щиты!"), true);
            }
        }

        // 6. SOULBINDER: Phantoms spawn more frequently around Soulbinder
        if (role == Role.SOULBINDER && tickCount % 600 == 0) {
            long clockTime = player.level().getOverworldClockTime() % 24000;
            boolean isNight = clockTime >= 13000 && clockTime <= 23000;
            if (isNight && player.level().canSeeSky(player.blockPosition())) {
                net.minecraft.world.entity.EntityType<?> phantomType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:phantom"));
                if (phantomType != null && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    phantomType.spawn(serverLevel, player.blockPosition().above(18), net.minecraft.world.entity.EntitySpawnReason.EVENT);
                }
            }
        }

        // DELVER: Haste underground
        if (role == Role.DELVER) {
            if (player.getY() < 50) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 0, true, false, false));
            } else {
                long clockTime = player.level().getOverworldClockTime() % 24000;
                boolean isDayTime = clockTime < 12000;
                if (isDayTime && player.level().canSeeSky(player.blockPosition())) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, false));
                }
            }
        }

        // GUARDIAN: Hunger drain
        if (role == Role.GUARDIAN) {
            if (tickCount % 80 == 0 && player.getFoodData().getFoodLevel() > 0) {
                player.getFoodData().addExhaustion(0.3f);
            }
        }

        // SCOUT: Restrict diamond/netherite armor
        if (role == Role.SCOUT) {
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
