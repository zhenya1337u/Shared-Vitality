package net.sharedvitality.compat;

import net.blay09.mods.hardcorerevival.api.HardcoreRevivalAPI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.sharedvitality.pool.HealthPoolManager;
import net.sharedvitality.role.Role;
import net.sharedvitality.role.RoleManager;

import java.util.List;

public class HardcoreRevivalCompat {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("hardcorerevival");

    public static boolean isLoaded() {
        return LOADED;
    }

    public static boolean isKnockedOut(ServerPlayer player) {
        if (!LOADED) return false;
        try {
            return HardcoreRevivalAPI.isKnockedOut(player);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Handles team downed state when pool health reaches 0.
     * If Hardcore Revival is present:
     * - The victim (or all players if everyone is critical) enters knockout.
     * - If everyone is knocked out, game over.
     * - If someone is still alive, pool is granted a grace period (1 HP).
     */
    public static boolean handlePoolDepleted(ServerPlayer victim, List<ServerPlayer> players) {
        if (!LOADED) return false;

        try {
            DamageSource damageSource = victim.damageSources().genericKill();

            // Knock out the player that took the fatal hit
            if (!HardcoreRevivalAPI.isKnockedOut(victim)) {
                HardcoreRevivalAPI.knockout(victim, damageSource);
            }

            // Check how many players are still active
            long activePlayers = players.stream().filter(p -> !HardcoreRevivalAPI.isKnockedOut(p)).count();

            if (activePlayers == 0) {
                // Total squad wipe!
                for (ServerPlayer player : players) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                1, 0, 0, 0, 0);
                    }
                    player.kill(player.level());
                    player.sendSystemMessage(Component.literal("§c☠ [Кровная связь] Весь отряд пал в бою! Игру окончено."));
                }
                HealthPoolManager.resetPoolAfterWipe();
                return true;
            } else {
                // Team still has conscious players to rescue downed allies!
                victim.sendSystemMessage(Component.literal("§c🩸 Вы тяжело ранены и упали в нокдаун! Союзники могут вас поднять!"));
                for (ServerPlayer ally : players) {
                    if (ally != victim) {
                        ally.sendSystemMessage(Component.literal("§e⚠ [SOS] " + victim.getName().getString() + " тяжело ранен! Помогите ему подняться!"));
                    }
                }
                // Maintain pool at 1 HP so other players don't immediately die while rescuing
                HealthPoolManager.setPoolHealthGrace(1.0f);
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Apothecary or ally reviving a knocked out player with Blood Bandage.
     * @return true if successfully rescued
     */
    public static boolean reviveWithBandage(ServerPlayer rescuer, ServerPlayer target) {
        if (!LOADED) return false;

        try {
            if (!HardcoreRevivalAPI.isKnockedOut(target)) {
                return false;
            }

            Role rescuerRole = RoleManager.getRole(rescuer.getUUID());
            boolean isApothecary = (rescuerRole == Role.APOTHECARY);

            // Apothecary instantly revives and restores 30% HP; other roles restore 15% HP
            HardcoreRevivalAPI.wakeup(target, true);

            float restoreRatio = isApothecary ? 0.35f : 0.20f;
            HealthPoolManager.heal(HealthPoolManager.getMaxHp() * restoreRatio);

            target.sendSystemMessage(Component.literal("§a❤ Вас поднял " + rescuer.getName().getString() + " с помощью Кровавого бинта!"));
            rescuer.sendSystemMessage(Component.literal("§a❤ Вы успешно подняли " + target.getName().getString() + "!"));
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
