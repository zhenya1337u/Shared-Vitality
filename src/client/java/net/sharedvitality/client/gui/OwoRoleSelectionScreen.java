package net.sharedvitality.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.sharedvitality.client.hud.SharedVitalityHud;
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
        FlowLayout dialog = UIContainers.verticalFlow(Sizing.fixed(340), Sizing.fixed(230));
        dialog.surface(Surface.PANEL.and(Surface.outline(0xFF8B0000)));
        dialog.padding(Insets.of(10));
        dialog.horizontalAlignment(HorizontalAlignment.CENTER);

        // Header Title
        dialog.child(
                UIComponents.label(Component.literal("§4⚔ §cКРОВНАЯ СВЯЗЬ §4⚔"))
                        .shadow(true)
                        .margins(Insets.bottom(2))
        );

        boolean alreadyChosen = SharedVitalityHud.hasChosenRole();
        String subtitle = alreadyChosen
                ? "§cВыбор заблокирован: вы уже выбрали роль!"
                : "§7Выберите архетип (роль выбирается только 1 раз):";

        dialog.child(
                UIComponents.label(Component.literal(subtitle))
                        .shadow(true)
                        .margins(Insets.bottom(6))
        );

        // Scrollable list of all 10 roles
        FlowLayout rolesList = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        rolesList.padding(Insets.right(4));

        // 1. Берсерк (Reaper)
        addRoleCard(rolesList, Role.REAPER, new ItemStack(Items.NETHERITE_SWORD),
                "§cБерсерк",
                "§aУрон растет при низком пуле команды (<30%: Сила II + Скорость I).\n" +
                "§aВампиризм: каждый 3-й критический удар лечит +2 HP в общий пул!\n" +
                "§cЕда восполняет только голод (не лечит напрямую).\n" +
                "§cДлительность горения в огне/лаве увеличена на 50%.", alreadyChosen);

        // 2. Маг (Enchanter)
        addRoleCard(rolesList, Role.ENCHANTER, new ItemStack(Items.ENCHANTED_BOOK),
                "§9Маг",
                "§a+50% сфер опыта, опыт автоматически делится с союзниками (32 бл).\n" +
                "§aКнижная аура (8 бл): усиливает союзников стихийной защитой.\n" +
                "§cБазовый запас брони снижен на 30%.\n" +
                "§cВзрывной урон (крипер/ТНТ) отнимает на 25% больше HP пула.", alreadyChosen);

        // 3. Строитель (Mason)
        addRoleCard(rolesList, Role.MASON, new ItemStack(Items.BRICKS),
                "§6Строитель",
                "§a+2 блока к дальности установки и разрушения.\n" +
                "§aКаменная цитадель: аура Стойкости I (Resistance I) союзникам на базе (16 бл от верстака).\n" +
                "§cПлывёт в воде на 40% медленнее.\n" +
                "§cВне базы шкала голода тратится на 20% быстрее.", alreadyChosen);

        // 4. Приручатель (Beastmaster)
        addRoleCard(rolesList, Role.BEASTMASTER, new ItemStack(Items.BONE),
                "§2Приручатель",
                "§aПрирученные животные получают x2 HP и скорость.\n" +
                "§aНейтральные мобы (големы, пчёлы, свинозомби) не атакуют первыми.\n" +
                "§cВ пещерах без неба получает дебафф замедления атаки.", alreadyChosen);

        // 5. Ассасин (Shadow)
        addRoleCard(rolesList, Role.SHADOW, new ItemStack(Items.ENDER_EYE),
                "§8Ассасин",
                "§aНевидимость для монстров при ходьбе на шифте (радиус 2 бл).\n" +
                "§aУдар в спину: гарантированный крит и оглушение цели на 1.5 сек.\n" +
                "§cСнижает общий пул команды на 6 HP.\n" +
                "§cЗапрет на использование щитов.", alreadyChosen);

        // 6. Шаман (Soulbinder)
        addRoleCard(rolesList, Role.SOULBINDER, new ItemStack(Items.TOTEM_OF_UNDYING),
                "§5Шаман",
                "§aДуховный щит: летальный удар блокируется на 1 HP + 5 сек неуязвимости команде (кд 10 мин)!\n" +
                "§cНаносит на 30% меньше урона оружием.\n" +
                "§cАура: вокруг Шамана чаще спавнятся фантомы.", alreadyChosen);

        // 7. Танк (Guardian)
        addRoleCard(rolesList, Role.GUARDIAN, new ItemStack(Items.SHIELD),
                "§6Танк",
                "§a+10 к пулу HP, блок 50% урона щитом за команду, стойкость к отталкиванию.\n" +
                "§cРасход шкалы голода на 25% быстрее.", alreadyChosen);

        // 8. Лекарь (Apothecary)
        addRoleCard(rolesList, Role.APOTHECARY, new ItemStack(Items.POTION),
                "§dЛекарь",
                "§aКровавые бинты восстанавливают 8 HP вместо 5 HP.\n" +
                "§aМгновенное поднятие союзника в нокдауне бинтом (+35% HP пулу)!\n" +
                "§cНаносит на 40% меньше физического урона оружием.", alreadyChosen);

        // 9. Шахтер (Delver)
        addRoleCard(rolesList, Role.DELVER, new ItemStack(Items.IRON_PICKAXE),
                "§eШахтер",
                "§aПостоянная Спешка (Haste I) под землей (Y < 50).\n" +
                "§cСлабость (Weakness I) под открытым небом в солнечный день.", alreadyChosen);

        // 10. Лучник (Scout)
        addRoleCard(rolesList, Role.SCOUT, new ItemStack(Items.BOW),
                "§bЛучник",
                "§a+15% скорость бега, повышенная дальность прыжка.\n" +
                "§c-4 HP от пула команды, запрет тяжелой брони (алмаз/незерит).", alreadyChosen);

        ScrollContainer<FlowLayout> scroll = UIContainers.verticalScroll(
                Sizing.fill(100), Sizing.fixed(150), rolesList);

        dialog.child(scroll);

        // Close button
        dialog.child(
                UIComponents.button(Component.literal("Закрыть"), btn -> this.onClose())
                        .margins(Insets.top(6))
        );

        rootComponent.child(dialog);
    }

    private void addRoleCard(FlowLayout container, Role role, ItemStack iconItem, String title, String description, boolean disabled) {
        FlowLayout card = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32));
        card.surface(Surface.PANEL_INSET);
        card.padding(Insets.of(3));
        card.margins(Insets.vertical(2));
        card.verticalAlignment(VerticalAlignment.CENTER);

        // Icon
        card.child(UIComponents.item(iconItem).margins(Insets.right(6)));

        // Title and description flow
        FlowLayout textLayout = UIContainers.verticalFlow(Sizing.fill(58), Sizing.content());
        textLayout.verticalAlignment(VerticalAlignment.CENTER);
        textLayout.child(UIComponents.label(Component.literal(title)).shadow(true));
        card.child(textLayout);

        // Tooltip with detailed pros and cons
        card.tooltip(List.of(
                Component.literal(title),
                Component.literal(description)
        ));

        // Select button
        var btn = UIComponents.button(Component.literal(disabled ? "Выбрано" : "Выбрать"), button -> {
            if (!disabled) {
                ClientPlayNetworking.send(new SelectRolePayload(role.name()));
                this.onClose();
            }
        });
        btn.margins(Insets.left(4));
        if (disabled) {
            btn.active = false;
        }

        card.child(btn);
        container.child(card);
    }
}
