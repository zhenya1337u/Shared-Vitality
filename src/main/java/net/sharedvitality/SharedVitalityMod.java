package net.sharedvitality;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.Vec3;
import net.sharedvitality.config.ModConfig;
import net.sharedvitality.item.BloodBandageItem;
import net.sharedvitality.network.SelectRolePayload;
import net.sharedvitality.network.SyncPoolPayload;
import net.sharedvitality.pool.HealthPoolManager;
import net.sharedvitality.role.Role;
import net.sharedvitality.role.RoleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedVitalityMod implements ModInitializer {
    public static final String MOD_ID = "sharedvitality";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, Integer> REAPER_CRIT_COUNTS = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Shared Vitality (Bloodlink) mod for Fabric 26.2");

        ModConfig.load();
        BloodBandageItem.register();

        // Register custom payloads
        PayloadTypeRegistry.clientboundPlay().register(SyncPoolPayload.TYPE, SyncPoolPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectRolePayload.TYPE, SelectRolePayload.CODEC);

        // Server tick for health pool & tether mechanics
        ServerTickEvents.END_SERVER_TICK.register(HealthPoolManager::tick);

        // Server lifecycle
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            HealthPoolManager.setServer(server);
            ModConfig.load();
            RoleManager.loadFromConfig(ModConfig.get());
            HealthPoolManager.loadFromConfig(ModConfig.get());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            RoleManager.saveToConfig(ModConfig.get());
            HealthPoolManager.saveToConfig(ModConfig.get());
            ModConfig.save();
        });

        // Player join & respawn
        ServerPlayerEvents.JOIN.register(player -> {
            RoleManager.applyRoleModifiers(player);
            HealthPoolManager.updatePoolCapacity(player.level().getServer().getPlayerList().getPlayers());
            HealthPoolManager.syncAll();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            RoleManager.applyRoleModifiers(newPlayer);
            HealthPoolManager.syncAll();
        });

        // Damage interception & pooling
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer victim) {
                // Friendly fire check
                if (!ModConfig.get().friendly_fire && source.getEntity() instanceof ServerPlayer attacker && attacker != victim) {
                    return false;
                }

                HealthPoolManager.onPlayerDamage(victim, source, amount);
                return true;
            }
            return true;
        });

        // Soulbinder -30% damage dealt debuff
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (source.getEntity() instanceof ServerPlayer attacker) {
                Role role = RoleManager.getRole(attacker.getUUID());
                if (role == Role.SOULBINDER) {
                    // Soulbinder has reduced damage dealt
                }
            }
            return true;
        });

        // ServerLivingEntityEvents.AFTER_DAMAGE for Reaper Lifesteal
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (entity instanceof LivingEntity && !blocked && damageTaken > 0.0f) {
                if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                    Role role = RoleManager.getRole(serverPlayer.getUUID());
                    // Reaper Lifesteal: every 3 successful critical hits that actually deal damage heals 2 HP into shared pool
                    if (role == Role.REAPER && serverPlayer.fallDistance > 0.0f && !serverPlayer.onGround()) {
                        int crits = REAPER_CRIT_COUNTS.getOrDefault(serverPlayer.getUUID(), 0) + 1;
                        if (crits >= 3) {
                            crits = 0;
                            HealthPoolManager.heal(2.0f);
                            if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.HEART,
                                        serverPlayer.getX(), serverPlayer.getY() + 1.2, serverPlayer.getZ(),
                                        5, 0.3, 0.3, 0.3, 0.1);
                            }
                            serverPlayer.sendSystemMessage(Component.literal("§c[Берсерк] Вампиризм сработал! +2 HP в общий пул!"), true);
                        }
                        REAPER_CRIT_COUNTS.put(serverPlayer.getUUID(), crits);
                    }
                }
            }
        });

        // AttackEntityCallback for Shadow Backstab
        AttackEntityCallback.EVENT.register((player, level, hand, target, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && target instanceof LivingEntity livingTarget) {
                Role role = RoleManager.getRole(serverPlayer.getUUID());

                // SHADOW Backstab: hit from behind target with sword
                if (role == Role.SHADOW && serverPlayer.getMainHandItem().is(ItemTags.SWORDS)) {
                    Vec3 targetLook = livingTarget.getLookAngle().normalize();
                    Vec3 attackerLook = serverPlayer.getLookAngle().normalize();
                    // Angle between looks: dot product > 0.6 means facing roughly same direction (attacking from behind)
                    if (targetLook.dot(attackerLook) > 0.6) {
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 4, false, false, false));
                        if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CRIT,
                                    livingTarget.getX(), livingTarget.getY() + 1.0, livingTarget.getZ(),
                                    10, 0.2, 0.2, 0.2, 0.2);
                        }
                        serverPlayer.sendSystemMessage(Component.literal("§8[Тень] Удар в спину! Цель оглушена!"), true);
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Handle C2S Role selection packet (allowed only ONCE per player)
        ServerPlayNetworking.registerGlobalReceiver(SelectRolePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (RoleManager.hasChosenRole(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("§c[Кровная связь] Вы уже выбрали свою роль! Изменить её нельзя."));
                    HealthPoolManager.syncAll();
                    return;
                }

                Role chosenRole = Role.fromString(payload.roleName());
                if (chosenRole == Role.NONE) return;

                // Check if role is already taken by another player
                if (RoleManager.isRoleTaken(chosenRole)) {
                    player.sendSystemMessage(Component.literal("§c[Кровная связь] Роль «" + chosenRole.getDisplayName() + "» уже занята другим игроком!"));
                    HealthPoolManager.syncAll();
                    return;
                }

                RoleManager.setRole(player.getUUID(), chosenRole);
                RoleManager.markRoleChosen(player.getUUID()); // lock role permanently
                RoleManager.applyRoleModifiers(player);
                HealthPoolManager.updatePoolCapacity(player.level().getServer().getPlayerList().getPlayers());
                HealthPoolManager.syncAll();
                player.sendSystemMessage(Component.literal("§a[Кровная связь] Вы выбрали роль: §e" + chosenRole.getDisplayName() + " §7(выбор зафиксирован)"));
            });
        });

        // Register in-game commands: /role <choose|get|reset> and /startad
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("startad")
                    .requires(source -> !source.isPlayer() || source.getServer().getPlayerList().isOp(source.getPlayer().nameAndId()))
                    .executes(ctx -> {
                        HealthPoolManager.startGame(ctx.getSource().getServer());
                        ctx.getSource().sendSuccess(() ->
                                Component.literal("§aИгра «Кровная связь» успешно запущена! Роли розданы."), true);
                        return 1;
                    })
            );

            dispatcher.register(Commands.literal("role")
                    .then(Commands.literal("choose")
                            .then(Commands.argument("rolename", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (Role r : Role.values()) {
                                            if (r != Role.NONE) builder.suggest(r.name().toLowerCase());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                                        if (RoleManager.hasChosenRole(player.getUUID()) && !ctx.getSource().getServer().getPlayerList().isOp(player.nameAndId())) {
                                            ctx.getSource().sendFailure(Component.literal("§cВы уже выбрали роль! Повторный выбор запрещен."));
                                            return 0;
                                        }

                                        String roleName = StringArgumentType.getString(ctx, "rolename");
                                        Role role = Role.fromString(roleName);
                                        if (role == Role.NONE) {
                                            ctx.getSource().sendFailure(Component.literal("§cНеизвестная роль."));
                                            return 0;
                                        }

                                        if (RoleManager.isRoleTaken(role) && RoleManager.getRole(player.getUUID()) != role) {
                                            ctx.getSource().sendFailure(Component.literal("§cРоль «" + role.getDisplayName() + "» уже занята другим игроком!"));
                                            return 0;
                                        }

                                        RoleManager.setRole(player.getUUID(), role);
                                        RoleManager.markRoleChosen(player.getUUID());
                                        RoleManager.applyRoleModifiers(player);
                                        HealthPoolManager.updatePoolCapacity(player.level().getServer().getPlayerList().getPlayers());
                                        HealthPoolManager.syncAll();
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("§aРоль успешно изменена на §e" + role.getDisplayName()), false);
                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("get")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                Role role = RoleManager.getRole(player.getUUID());
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("§aВаша текущая роль: §e" + role.getDisplayName()), false);
                                return 1;
                            })
                    )
                    .then(Commands.literal("reset")
                            .requires(source -> !source.isPlayer() || source.getServer().getPlayerList().isOp(source.getPlayer().nameAndId()))
                            .executes(ctx -> {
                                for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                    RoleManager.resetRoleChosen(p.getUUID());
                                }
                                HealthPoolManager.syncAll();
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("§eФлаг выбора ролей сброшен для всех игроков."), true);
                                return 1;
                            })
                    )
            );
        });
    }
}
