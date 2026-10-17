package net.sharedvitality.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.sharedvitality.network.SelectRolePayload;
import net.sharedvitality.role.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OwoRoleSelectionScreen extends BaseOwoScreen<FlowLayout> {

    public OwoRoleSelectionScreen() {
        super(Component.literal("Кровная связь — Выбор роли"));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x77000000));
        rootComponent.verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER);

        // Main modal dialog container
        FlowLayout dialog = UIContainers.verticalFlow(Sizing.fixed(320), Sizing.content());
        dialog.surface(Surface.PANEL.and(Surface.outline(0xFF8B0000)));
        dialog.padding(Insets.of(12));
        dialog.horizontalAlignment(HorizontalAlignment.CENTER);

        // Header Title
        dialog.child(
                UIComponents.label(Component.literal("§4⚔ §cКРОВНАЯ СВЯЗЬ §4⚔"))
                        .shadow(true)
                        .margins(Insets.bottom(4))
        );

        dialog.child(
                UIComponents.label(Component.literal("§7Выберите архетип для своей группы:"))
                        .shadow(true)
                        .margins(Insets.bottom(10))
        );

        // Role Cards list
        addRoleCard(dialog, Role.GUARDIAN, new ItemStack(Items.SHIELD),
                "§6Страж (Guardian)",
                "§a+10 к общему пулу HP, блок 50% урона щитом, стойкость к отталкиванию.\n§cРасход шкалы голода на 25% быстрее.");

        addRoleCard(dialog, Role.APOTHECARY, new ItemStack(Items.POTION),
                "§dАлхимик / Повар (Apothecary)",
                "§aБинты и еда восстанавливают на 30-60% больше здоровья и сытости.\n§cНаносит на 40% меньше физического урона.");

        addRoleCard(dialog, Role.DELVER, new ItemStack(Items.IRON_PICKAXE),
                "§eГеолог (Delver)",
                "§aПостоянная Спешка (Haste I) под землей (Y < 50), удача при добыче.\n§cСлабость (Weakness I) под открытым небом днем.");

        addRoleCard(dialog, Role.SCOUT, new ItemStack(Items.BOW),
                "§bСледопыт (Scout)",
                "§a+15% скорость бега, увеличенный прыжок, урон лука на дистанции.\n§c-4 HP от пула команды, запрет тяжелой брони.");

        // Close button
        dialog.child(
                UIComponents.button(Component.literal("Закрыть"), btn -> this.onClose())
                        .margins(Insets.top(8))
        );

        rootComponent.child(dialog);
    }

    private void addRoleCard(FlowLayout container, Role role, ItemStack iconItem, String title, String description) {
        FlowLayout card = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(36));
        card.surface(Surface.PANEL_INSET);
        card.padding(Insets.of(4));
        card.margins(Insets.vertical(2));
        card.verticalAlignment(VerticalAlignment.CENTER);

        // Icon
        card.child(UIComponents.item(iconItem).margins(Insets.right(6)));

        // Title and description flow
        FlowLayout textLayout = UIContainers.verticalFlow(Sizing.fill(60), Sizing.content());
        textLayout.verticalAlignment(VerticalAlignment.CENTER);
        textLayout.child(UIComponents.label(Component.literal(title)).shadow(true));
        card.child(textLayout);

        // Tooltip with detailed pros and cons
        card.tooltip(List.of(
                Component.literal(title),
                Component.literal(description)
        ));

        // Select button
        card.child(
                UIComponents.button(Component.literal("Выбрать"), btn -> {
                    ClientPlayNetworking.send(new SelectRolePayload(role.name()));
                    this.onClose();
                }).margins(Insets.left(4))
        );

        container.child(card);
    }
}
