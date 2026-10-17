package net.sharedvitality.pool;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.sharedvitality.config.ModConfig;
import net.sharedvitality.network.SyncPoolPayload;
import net.sharedvitality.role.Role;
import net.sharedvitality.role.RoleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HealthPoolManager {
    private static float currentHp = 20.0f;
    private static float maxHp = 20.0f;
    private static boolean initialized = false;
    private static boolean isDistributingDamage = false;
    private static boolean gameStarted = false;
    private static MinecraftServer serverInstance;

    // Soulbinder spiritual shield cooldown: 10 minutes (12000 ticks)
    private static int soulbinderShieldCooldown = 0;
    private static int invulnerabilityTicks = 0;

    public static void setServer(MinecraftServer server) {
        serverInstance = server;
    }

    public static void setGameStarted(boolean started) {
        gameStarted = started;
        if (started) {
            initialized = false;
            soulbinderShieldCooldown = 0;
            invulnerabilityTicks = 0;
            if (serverInstance != null) {
                updatePoolCapacity(serverInstance.getPlayerList().getPlayers());
                currentHp = maxHp;
            }
        }
        saveToConfig(ModConfig.get());
        ModConfig.save();
        syncAll();
    }

    public static void saveToConfig(ModConfig config) {
        config.saved_game_started = gameStarted;
        config.saved_current_hp = currentHp;
    }

    public static void loadFromConfig(ModConfig config) {
        gameStarted = config.saved_game_started;
        currentHp = config.saved_current_hp > 0 ? config.saved_current_hp : 20.0f;
    }

    public static float getCurrentHp() {
        return currentHp;
    }

    public static float getMaxHp() {
        return maxHp;
    }

    public static void startGame(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        // Prepare roles and shuffle
        List<Role> availableRoles = new ArrayList<>(List.of(
                Role.GUARDIAN, Role.APOTHECARY, Role.DELVER, Role.SCOUT,
                Role.REAPER, Role.ENCHANTER, Role.MASON, Role.BEASTMASTER,
                Role.SHADOW, Role.SOULBINDER
        ));
        Collections.shuffle(availableRoles);

        // Only assign random roles to players who haven't chosen a role yet
        List<Role> takenRoles = new ArrayList<>();
        for (ServerPlayer player : players) {
            Role current = RoleManager.getRole(player.getUUID());
            if (current != Role.NONE) {
                takenRoles.add(current);
            }
        }
        availableRoles.removeAll(takenRoles);

        int randomRoleIndex = 0;
        for (ServerPlayer player : players) {
            Role current = RoleManager.getRole(player.getUUID());
            if (current == Role.NONE) {
                Role role = (!availableRoles.isEmpty())
                        ? availableRoles.get(randomRoleIndex % availableRoles.size())
                        : Role.GUARDIAN;
                randomRoleIndex++;
                RoleManager.setRole(player.getUUID(), role);
            }
            RoleManager.markRoleChosen(player.getUUID()); // lock role so it cannot be changed in menu
            RoleManager.applyRoleModifiers(player);
        }

        setGameStarted(true);

        for (ServerPlayer player : players) {
            Role role = RoleManager.getRole(player.getUUID());
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE),
                    SoundSource.PLAYERS,
                    player.getX(), player.getY(), player.getZ(),
                    1.0f, 1.0f, player.getRandom().nextLong()
            ));

            player.sendSystemMessage(Component.literal("§6========================================"));
            player.sendSystemMessage(Component.literal("§c⚔ ИГРА «КРОВНАЯ СВЯЗЬ» НАЧАЛАСЬ! ⚔"));
            player.sendSystemMessage(Component.literal("§aВаша роль: §e" + role.getDisplayName()));
            player.sendSystemMessage(Component.literal("§7Общий пул здоровья активирован. Держитесь вместе!"));
            player.sendSystemMessage(Component.literal("§6========================================"));
        }

        syncAll();
    }

    public static void updatePoolCapacity(List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        double baseScaling = ModConfig.get().pool_scaling_per_player;
        float calculatedMax = (float) (players.size() * baseScaling);

        for (ServerPlayer player : players) {
            Role role = RoleManager.getRole(player.getUUID());
            if (role == Role.GUARDIAN) {
                calculatedMax += 10.0f;
            } else if (role == Role.SCOUT) {
                calculatedMax = Math.max(1.0f, calculatedMax - 4.0f);
            } else if (role == Role.SHADOW) {
                calculatedMax = Math.max(1.0f, calculatedMax - 6.0f);
            }
        }

        if (!initialized) {
            currentHp = calculatedMax;
            initialized = true;
        } else if (currentHp > calculatedMax) {
            currentHp = calculatedMax;
        }
        maxHp = calculatedMax;
    }

    public static void heal(float amount) {
        if (!gameStarted) return;
        currentHp = Math.min(maxHp, currentHp + amount);
        syncAll();
    }

    public static void onPlayerDamage(ServerPlayer victim, DamageSource source, float amount) {
        if (!gameStarted || isDistributingDamage || serverInstance == null) return;
        if (invulnerabilityTicks > 0) return; // Team is invulnerable from Soulbinder shield

        List<ServerPlayer> players = serverInstance.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        Role victimRole = RoleManager.getRole(victim.getUUID());

        // Guardian absorbs 50% damage if blocking with shield
        if (victimRole == Role.GUARDIAN && victim.isBlocking()) {
            amount *= 0.5f;
        }

        // Enchanter: direct explosive damage loses 25% more pool HP
        if (victimRole == Role.ENCHANTER && source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= 1.25f;
        }

        // Check if lethal hit and team has Soulbinder with ready Spiritual Shield
        if (currentHp - amount <= 0.001f) {
            boolean hasSoulbinder = false;
            for (ServerPlayer p : players) {
                if (RoleManager.getRole(p.getUUID()) == Role.SOULBINDER) {
                    hasSoulbinder = true;
                    break;
                }
            }
            if (hasSoulbinder && soulbinderShieldCooldown <= 0) {
                // Soulbinder saves the team!
                currentHp = 1.0f;
                invulnerabilityTicks = 100; // 5 seconds of total invulnerability
                soulbinderShieldCooldown = 12000; // 10 minutes cooldown

                for (ServerPlayer p : players) {
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.TOTEM_USE),
                            SoundSource.PLAYERS,
                            p.getX(), p.getY(), p.getZ(),
                            1.0f, 1.0f, p.getRandom().nextLong()
                    ));
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, true, false, false));
                    p.sendSystemMessage(Component.literal("§d✨ [ДУХОВНЫЙ ЩИТ ШАМАНА] Смертельный удар поглощен! 5 секунд неуязвимости!"));
                }
                syncAll();
                return;
            }
        }

        isDistributingDamage = true;
        try {
            currentHp = Math.max(0.0f, currentHp - amount);

            for (ServerPlayer player : players) {
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(net.sharedvitality.SharedVitalityMod.HEARTBEAT),
                        SoundSource.PLAYERS,
                        player.getX(), player.getY(), player.getZ(),
                        1.0f, 1.0f, player.getRandom().nextLong()
                ));

                if (player != victim) {
                    player.level().sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            2, 0.2, 0.2, 0.2, 0.05);
                }
            }

            if (currentHp <= 0.001f) {
                handleTeamDownedOrDeath(victim, players);
            }
        } finally {
            isDistributingDamage = false;
        }

        syncAll();
    }

    public static void setPoolHealthGrace(float graceHp) {
        currentHp = graceHp;
        syncAll();
    }

    public static void resetPoolAfterWipe() {
        currentHp = maxHp;
        syncAll();
    }

    private static void handleTeamDownedOrDeath(ServerPlayer victim, List<ServerPlayer> players) {
        if (net.sharedvitality.compat.HardcoreRevivalCompat.isLoaded()) {
            boolean handled = net.sharedvitality.compat.HardcoreRevivalCompat.handlePoolDepleted(victim, players);
            if (handled) return;
        }

        for (ServerPlayer player : players) {
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        1, 0, 0, 0, 0);
            }
            player.kill(player.level());
            player.sendSystemMessage(Component.literal("§c[Кровная связь] Общий пул здоровья команды иссяк!"));
        }
        currentHp = maxHp;
    }

    public static void tick(MinecraftServer server) {
        if (!gameStarted) return;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        updatePoolCapacity(players);

        int tickCount = server.getTickCount();

        if (soulbinderShieldCooldown > 0) {
            soulbinderShieldCooldown--;
        }
        if (invulnerabilityTicks > 0) {
            invulnerabilityTicks--;
        }

        // Natural slow regen if not severed
        if (tickCount % 40 == 0 && currentHp < maxHp) {
            heal(0.5f);
        }

        // Tether calculation
        double warnDist = ModConfig.get().tether_distance_warning;
        double severDist = ModConfig.get().tether_distance_sever;

        ServerPlayer anchor = players.get(0);
        boolean crossDim = false;
        double maxDist = 0;

        for (ServerPlayer p : players) {
            RoleManager.tickRole(p);

            if (p.level().dimension() != anchor.level().dimension()) {
                crossDim = true;
            } else {
                double dist = p.distanceTo(anchor);
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
        }

        int warningLevel = 0;
        if (crossDim) {
            warningLevel = 2; // Severed
            if (ModConfig.get().cross_dimension_decay && tickCount % 100 == 0) {
                currentHp = Math.max(1.0f, currentHp - 1.0f);
                for (ServerPlayer p : players) {
                    p.sendSystemMessage(Component.literal("§c[Связь нарушена] Разные измерения медленно сжигают общее HP!"), true);
                }
            }
        } else if (maxDist > severDist) {
            warningLevel = 2;
            if (tickCount % 60 == 0) {
                for (ServerPlayer p : players) {
                    p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, true, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, true, false, false));
                }
            }
        } else if (maxDist > warnDist) {
            warningLevel = 1; // Tension
        }

        if (tickCount % 20 == 0) {
            syncTether(warningLevel);
        }
    }

    public static void syncAll() {
        syncTether(0);
    }

    public static void syncTether(int warningLevel) {
        if (serverInstance == null) return;
        List<ServerPlayer> players = serverInstance.getPlayerList().getPlayers();
        int total = players.size();
        int ready = 0;

        // Build simple token string for taken roles: ROLE1:PlayerName,ROLE2:PlayerName
        StringBuilder takenSb = new StringBuilder();
        for (Role r : Role.values()) {
            if (r == Role.NONE) continue;
            String ownerName = RoleManager.getPlayerNameForRole(r);
            if (ownerName != null) {
                if (takenSb.length() > 0) takenSb.append(";");
                takenSb.append(r.name()).append(":").append(ownerName);
            }
        }
        String takenStr = takenSb.toString();

        for (ServerPlayer player : players) {
            RoleManager.updatePlayerName(player.getUUID(), player.getName().getString());
            if (RoleManager.hasChosenRole(player.getUUID())) {
                ready++;
            }
        }

        for (ServerPlayer player : players) {
            Role role = RoleManager.getRole(player.getUUID());
            String roleName = gameStarted ? role.getDisplayName() : "Ожидание старта (/startad)";
            boolean chosen = RoleManager.hasChosenRole(player.getUUID());
            ServerPlayNetworking.send(player, new SyncPoolPayload(
                    currentHp, maxHp, roleName, warningLevel, chosen, ready, total, takenStr
            ));
        }
    }
}
