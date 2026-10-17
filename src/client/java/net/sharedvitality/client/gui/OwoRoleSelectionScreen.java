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

import java.util.ArrayList;
import java.util.List;

public class OwoRoleSelectionScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x88000000));
        rootComponent.verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER);

        // Spacious modal dialog container (480px wide, 270px high)
        FlowLayout dialog = UIContainers.verticalFlow(Sizing.fixed(480), Sizing.fixed(270));
        dialog.surface(Surface.PANEL.and(Surface.outline(0xFF8B0000)));
        dialog.padding(Insets.of(8));
        dialog.horizontalAlignment(HorizontalAlignment.CENTER);

        // Header Title
        dialog.child(
                UIComponents.label(Component.literal("§4⚔ §cКРОВНАЯ СВЯЗЬ: ВЫБОР РОЛИ §4⚔"))
                        .shadow(true)
                        .margins(Insets.bottom(2))
        );

        boolean alreadyChosen = SharedVitalityHud.hasChosenRole();
        String subtitle = alreadyChosen
                ? "§cВыбор заблокирован: вы уже выбрали роль!"
                : "§7Выберите свой класс для команды (роль закрепляется на игру):";

        dialog.child(
                UIComponents.label(Component.literal(subtitle))
                        .shadow(true)
                        .margins(Insets.bottom(4))
        );

        // Scrollable list of all 10 roles
        FlowLayout rolesList = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        rolesList.padding(Insets.right(6));

        // 1. Танк
        addRoleCard(rolesList, Role.GUARDIAN, new ItemStack(Items.SHIELD),
                "§6Танк (Guardian)",
                "§a• +10 HP к общему пулу команды.\n" +
                "§a• Блок щитом поглощает 50% командного урона.\n" +
                "§a• Высокая стойкость к отбрасыванию.",
                "§c• Шкала голода тратится на 25% быстрее.",
                alreadyChosen);

        // 2. Лекарь
        addRoleCard(rolesList, Role.APOTHECARY, new ItemStack(Items.POTION),
                "§dЛекарь (Apothecary)",
                "§a• Кровавые бинты восстанавливают 8 HP вместо 5 HP.\n" +
                "§a• ПКМ бинтом мгновенно поднимает союзника из нокдауна (+35% HP пулу)!",
                "§c• Наносит на 40% меньше физического урона оружием.",
                alreadyChosen);

        // 3. Шахтер
        addRoleCard(rolesList, Role.DELVER, new ItemStack(Items.IRON_PICKAXE),
                "§eШахтер (Delver)",
                "§a• Постоянная Спешка (Haste I) под землей ниже Y=50.\n" +
                "§a• Увеличенный шанс добычи редких руд.",
                "§c• Слабость (Weakness I) под открытым небом в солнечный день.",
                alreadyChosen);

        // 4. Лучник
        addRoleCard(rolesList, Role.SCOUT, new ItemStack(Items.BOW),
                "§bЛучник (Scout)",
                "§a• Скорость бега выше на 15%, увеличенная дальность прыжка.\n" +
                "§a• Идеальный разведчик и стрелок отряда.",
                "§c• Снижает общий пул на 4 HP. Запрет на тяжёлую броню.",
                alreadyChosen);

        // 5. Берсерк
        addRoleCard(rolesList, Role.REAPER, new ItemStack(Items.NETHERITE_SWORD),
                "§cБерсерк (Reaper)",
                "§a• При HP пула <30% получает постоянные Силу II и Скорость I.\n" +
                "§a• Вампиризм: каждые 3 прыжковых крита лечат +2 HP в общий пул!",
                "§c• Еда не восстанавливает здоровье напрямую. Горение на 50% дольше.",
                alreadyChosen);

        // 6. Маг
        addRoleCard(rolesList, Role.ENCHANTER, new ItemStack(Items.ENCHANTED_BOOK),
                "§9Маг (Enchanter)",
                "§a• +50% сфер опыта, делится опытом с союзниками в 32 блоках.\n" +
                "§a• Книжная аура (8 блоков): дарует стихийную защиту союзникам.",
                "§c• Броня слабее на 30%. Взрывы отнимают на 25% больше HP пула.",
                alreadyChosen);

        // 7. Строитель
        addRoleCard(rolesList, Role.MASON, new ItemStack(Items.BRICKS),
                "§6Строитель (Mason)",
                "§a• +2 блока к дистанции установки и разрушения блоков.\n" +
                "§a• Цитадель: аура Стойкости I союзникам на базе (16 блоков от верстака).",
                "§c• Плавает в воде на 40% медленнее. Вне базы голод тратится быстрее.",
                alreadyChosen);

        // 8. Приручатель
        addRoleCard(rolesList, Role.BEASTMASTER, new ItemStack(Items.BONE),
                "§2Приручатель (Beastmaster)",
                "§a• Прирученные животные получают x2 здоровья и увеличенную скорость.\n" +
                "§a• Нейтральные мобы (големы, пчёлы, свинозомби) не нападают первыми.",
                "§c• В темных пещерах получает усталость при добыче блоков.",
                alreadyChosen);

        // 9. Ассасин
        addRoleCard(rolesList, Role.SHADOW, new ItemStack(Items.ENDER_EYE),
                "§8Ассасин (Shadow)",
                "§a• Полная невидимость для мобов при передвижении на Shift.\n" +
                "§a• Удар мечом в спину: гарантированный крит и оглушение цели на 1.5 сек.",
                "§c• Снижает общий пул отряда на 6 HP. Запрещено носить щит.",
                alreadyChosen);

        // 10. Шаман
        addRoleCard(rolesList, Role.SOULBINDER, new ItemStack(Items.TOTEM_OF_UNDYING),
                "§5Шаман (Soulbinder)",
                "§a• Духовный щит: блокирует смертельный удар отряда (кд 10 мин) + 5 сек бессмертия!\n" +
                "§a• Спасает всю команду от верной гибели в критический момент.",
                "§c• Наносит на 30% меньше урона. Привлекает ночных фантомов.",
                alreadyChosen);

        ScrollContainer<FlowLayout> scroll = UIContainers.verticalScroll(
                Sizing.fill(100), Sizing.fixed(200), rolesList);

        dialog.child(scroll);

        // Close button
        dialog.child(
                UIComponents.button(Component.literal("Закрыть"), btn -> this.onClose())
                        .margins(Insets.top(4))
        );

        rootComponent.child(dialog);
    }

    private void addRoleCard(FlowLayout container, Role role, ItemStack iconItem, String title, String pros, String cons, boolean disabled) {
        // High card (48px) with plenty of room for 2-line descriptions
        FlowLayout card = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(48));
        card.surface(Surface.PANEL_INSET);
        card.padding(Insets.of(4));
        card.margins(Insets.vertical(2));
        card.verticalAlignment(VerticalAlignment.CENTER);

        // Big Icon on the left
        card.child(UIComponents.item(iconItem).margins(Insets.right(8)));

        // Middle layout: Title + 1-2 line summary of perks and cons
        FlowLayout textLayout = UIContainers.verticalFlow(Sizing.fill(70), Sizing.content());
        textLayout.verticalAlignment(VerticalAlignment.CENTER);

        // Header Title
        textLayout.child(UIComponents.label(Component.literal(title)).shadow(true));

        // Short preview line from pros and cons
        String firstPro = pros.split("\n")[0].replace("• ", "");
        String firstCon = cons.split("\n")[0].replace("• ", "");
        textLayout.child(
                UIComponents.label(Component.literal(firstPro + " " + firstCon))
                        .shadow(true)
                        .margins(Insets.top(1))
        );

        card.child(textLayout);

        // Rich Tooltip with full breakdown of pros and cons
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.literal(title));
        tooltipLines.add(Component.literal("§aПреимущества:"));
        for (String line : pros.split("\n")) {
            tooltipLines.add(Component.literal(line));
        }
        tooltipLines.add(Component.literal("§cОграничения:"));
        for (String line : cons.split("\n")) {
            tooltipLines.add(Component.literal(line));
        }
        card.tooltip(tooltipLines);

        // Select button on the right
        var btn = UIComponents.button(Component.literal(disabled ? "Выбрано" : "Выбрать"), button -> {
            if (!disabled) {
                ClientPlayNetworking.send(new SelectRolePayload(role.name()));
                this.onClose();
            }
        });
        btn.margins(Insets.left(6));
        if (disabled) {
            btn.active = false;
        }

        card.child(btn);
        container.child(card);
    }
}
