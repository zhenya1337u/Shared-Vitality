package net.sharedvitality;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.sharedvitality.config.ModConfig;
import net.sharedvitality.item.BloodBandageItem;
import net.sharedvitality.network.SelectRolePayload;
import net.sharedvitality.network.SyncPoolPayload;
import net.sharedvitality.pool.HealthPoolManager;
import net.sharedvitality.role.Role;
import net.sharedvitality.role.RoleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharedVitalityMod implements ModInitializer {
    public static final String MOD_ID = "sharedvitality";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
        ServerLifecycleEvents.SERVER_STARTING.register(HealthPoolManager::setServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ModConfig.save());

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

                HealthPoolManager.onPlayerDamage(victim, amount);
                return true;
            }
            return true;
        });

        // Handle C2S Role selection packet
        ServerPlayNetworking.registerGlobalReceiver(SelectRolePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            Role chosenRole = Role.fromString(payload.roleName());
            context.server().execute(() -> {
                RoleManager.setRole(player.getUUID(), chosenRole);
                RoleManager.applyRoleModifiers(player);
                HealthPoolManager.updatePoolCapacity(player.level().getServer().getPlayerList().getPlayers());
                HealthPoolManager.syncAll();
                player.sendSystemMessage(Component.literal("§a[Кровная связь] Вы выбрали роль: §e" + chosenRole.getDisplayName()));
            });
        });

        // Register in-game command: /role <choose|get> and /startad
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
                                        String roleName = StringArgumentType.getString(ctx, "rolename");
                                        Role role = Role.fromString(roleName);
                                        RoleManager.setRole(player.getUUID(), role);
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
            );
        });
    }
}
