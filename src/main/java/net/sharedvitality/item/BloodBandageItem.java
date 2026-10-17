package net.sharedvitality.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sharedvitality.SharedVitalityMod;
import net.sharedvitality.pool.HealthPoolManager;
import net.sharedvitality.role.Role;
import net.sharedvitality.role.RoleManager;

public class BloodBandageItem extends Item {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "blood_bandage");
    public static final ResourceKey<Item> KEY = ResourceKey.create(Registries.ITEM, ID);

    public BloodBandageItem(Properties properties) {
        super(properties.setId(KEY).stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            Role role = RoleManager.getRole(player.getUUID());
            float healAmount = (role == Role.APOTHECARY) ? 8.0f : 5.0f;

            HealthPoolManager.heal(healAmount);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0f, 1.2f);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            player.getCooldowns().addCooldown(itemStack, 60);
        }
        return InteractionResult.SUCCESS;
    }

    public static BloodBandageItem register() {
        return Registry.register(BuiltInRegistries.ITEM, ID, new BloodBandageItem(new Properties()));
    }
}
