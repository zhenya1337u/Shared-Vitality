package net.sharedvitality.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.sharedvitality.client.hud.SharedVitalityHud;
import net.sharedvitality.network.SelectRolePayload;
import net.sharedvitality.role.Role;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OwoRoleSelectionScreen extends BaseOwoScreen<FlowLayout> {

    private ScrollContainer<FlowLayout> scrollContainer;
    private double currentScrollOffset = 0;
    private final Map<Role, String> takenRolesMap = new HashMap<>();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    private void parseTakenRoles() {
        takenRolesMap.clear();
        String takenStr = SharedVitalityHud.getTakenRolesJson();
        if (takenStr != null && !takenStr.isBlank()) {
            for (String pair : takenStr.split(";")) {
                String[] parts = pair.split(":");
                if (parts.length >= 2) {
                    Role r = Role.fromString(parts[0]);
                    if (r != Role.NONE) {
                        takenRolesMap.put(r, parts[1]);
                    }
                }
            }
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        parseTakenRoles();

        rootComponent.surface(Surface.flat(0xEE090812)); // Sleek ultra-dark navy background
        rootComponent.verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER);
        rootComponent.padding(Insets.of(6));

        // 1. TOP HEADER: "ЛЕГИОН ОДНОЙ ДУШИ — ВЫБОР СУДЬБЫ"
        rootComponent.child(
                UIComponents.label(Component.literal("§f§lЛЕГИОН ОДНОЙ ДУШИ §7— §f§lВЫБОР СУДЬБЫ"))
                        .shadow(true)
                        .margins(Insets.bottom(1))
        );

        rootComponent.child(
                UIComponents.label(Component.literal("§8Листайте список ролей колесиком мыши (10 уникальных архетипов)"))
                        .shadow(true)
                        .margins(Insets.bottom(4))
        );

        // 2. MAIN CAROUSEL ROW (Scroll with mouse wheel / drag)
        FlowLayout carouselRow = UIContainers.horizontalFlow(Sizing.fill(98), Sizing.fixed(268));
        carouselRow.verticalAlignment(VerticalAlignment.CENTER);
        carouselRow.horizontalAlignment(HorizontalAlignment.CENTER);

        // Horizontal roles container
        FlowLayout cardsLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
        cardsLayout.padding(Insets.horizontal(4));
        cardsLayout.verticalAlignment(VerticalAlignment.CENTER);

        boolean playerAlreadyChosen = SharedVitalityHud.hasChosenRole();

        // 1. ТАНК
        createRoleCard(cardsLayout, Role.GUARDIAN, new ItemStack(Items.SHIELD),
                "ТАНК", "СТРАЖ ОТРЯДА", 0xFFE5A93C,
                List.of("+ +10 HP к пулу команды", "+ Блок щитом: 50% защиты всем", "+ Стойкость к отдаче"),
                List.of("- Расход голода быстрее на 25%"),
                playerAlreadyChosen);

        // 2. ЛЕКАРЬ
        createRoleCard(cardsLayout, Role.APOTHECARY, new ItemStack(Items.POTION),
                "ЛЕКАРЬ", "ВРАЧЕВАТЕЛЬ ДУШ", 0xFFE056FD,
                List.of("+ Бинты лечат 8 HP (вместо 5)", "+ Спасение из нокдауна бинтом", "+ +35% пула при реанимации"),
                List.of("- Урон оружием снижен на 40%"),
                playerAlreadyChosen);

        // 3. ШАХТЁР
        createRoleCard(cardsLayout, Role.DELVER, new ItemStack(Items.IRON_PICKAXE),
                "ШАХТЁР", "ХОЗЯИН НЕДР", 0xFFF1C40F,
                List.of("+ Спешка I ниже Y=50", "+ Повышенный шанс редких руд", "+ Скоростная добыча недр"),
                List.of("- Слабость днем под солнцем"),
                playerAlreadyChosen);

        // 4. ЛУЧНИК
        createRoleCard(cardsLayout, Role.SCOUT, new ItemStack(Items.BOW),
                "ЛУЧНИК", "ОХОТНИК", 0xFF3867D6,
                List.of("+ Скорость бега выше на 15%", "+ Увеличенная дальность прыжка", "+ Высокая мобильность в бою"),
                List.of("- Снижает пул отряда на 4 HP", "- Запрет на тяжёлую броню"),
                playerAlreadyChosen);

        // 5. БЕРСЕРК
        createRoleCard(cardsLayout, Role.REAPER, new ItemStack(Items.NETHERITE_SWORD),
                "БЕРСЕРК", "ЖНЕЦ КРОВИ", 0xFFEB3B5A,
                List.of("+ Сила II и Скорость I при <30% HP", "+ Вампиризм: 3 крита лечат пул", "+ Максимальный урон в бою"),
                List.of("- Еда не восстанавливает здоровье", "- Горение в огне на 50% дольше"),
                playerAlreadyChosen);

        // 6. МАГ
        createRoleCard(cardsLayout, Role.ENCHANTER, new ItemStack(Items.ENCHANTED_BOOK),
                "МАГ", "МИСТИК", 0xFF4B7BEC,
                List.of("+ +50% сфер опыта от мобов", "+ Делёж опыта на весь отряд (32 бл)", "+ Стихийная защита союзникам"),
                List.of("- Броня слабее на 30%", "- Уязвимость к взрывам (+25%)"),
                playerAlreadyChosen);

        // 7. СТРОИТЕЛЬ
        createRoleCard(cardsLayout, Role.MASON, new ItemStack(Items.BRICKS),
                "СТРОИТЕЛЬ", "АРХИТЕКТОР", 0xFFFA8231,
                List.of("+ Дальность стройки +2 блока", "+ Аура Стойкости на базе (16 бл)", "+ Быстрое возведение крепости"),
                List.of("- Медленный в воде (-40%)", "- Вне базы тратит больше сытости"),
                playerAlreadyChosen);

        // 8. ПРИРУЧАТЕЛЬ
        createRoleCard(cardsLayout, Role.BEASTMASTER, new ItemStack(Items.BONE),
                "ПРИРУЧАТЕЛЬ", "ПОВЕЛИТЕЛЬ ЗВЕРЕЙ", 0xFF20BF6B,
                List.of("+ Питомцы получают x2 HP и бег", "+ Нейтральные мобы не атакуют", "+ Мощная стая защитников"),
                List.of("- Усталость копания в пещерах"),
                playerAlreadyChosen);

        // 9. АССАСИН
        createRoleCard(cardsLayout, Role.SHADOW, new ItemStack(Items.ENDER_EYE),
                "АССАСИН", "ТЕНЬ", 0xFF778CA3,
                List.of("+ Полная невидимость на Shift", "+ Удар в спину: крит и стан 1.5с", "+ Идеальный охотник из засады"),
                List.of("- Снижает пул отряда на 6 HP", "- Запрещено носить щит"),
                playerAlreadyChosen);

        // 10. ШАМАН
        createRoleCard(cardsLayout, Role.SOULBINDER, new ItemStack(Items.TOTEM_OF_UNDYING),
                "ШАМАН", "ХРАНИТЕЛЬ УЗ", 0xFF8854D0,
                List.of("+ Духовный щит: спасение от вайпа", "+ 5 сек бессмертия отряду (кд 10м)", "+ Защищает связь всей команды"),
                List.of("- Наносит на 30% меньше урона", "- Спавнит фантомов по ночам"),
                playerAlreadyChosen);

        // Horizontal Scroll Container
        scrollContainer = UIContainers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), cardsLayout);
        scrollContainer.scrollbarThiccness(3);
        carouselRow.child(scrollContainer);

        rootComponent.child(carouselRow);

        // 3. BOTTOM PILL BAR: "ОТРЯД: X ИЗ N ИГРОКОВ ВСЕГО СЕРВЕРА • ОЖИДАНИЕ СТАРТА"
        int readyCount = SharedVitalityHud.getReadyCount();
        int totalPlayers = Math.max(1, SharedVitalityHud.getTotalPlayers());

        FlowLayout bottomPill = UIContainers.horizontalFlow(Sizing.content(), Sizing.fixed(22));
        bottomPill.surface(Surface.flat(0xCC11101E).and(Surface.outline(0xFF2C2B3E)));
        bottomPill.padding(Insets.both(12, 3));
        bottomPill.verticalAlignment(VerticalAlignment.CENTER);
        bottomPill.margins(Insets.top(6));

        String statusText = String.format("§fОТРЯД: §e%d §fИЗ §e%d §fИГРОКОВ СЕРВЕРА  •  §7ОЖИДАНИЕ СТАРТА (/startad)", readyCount, totalPlayers);
        bottomPill.child(UIComponents.label(Component.literal(statusText)).shadow(true));

        // Yellow status pulse dot
        bottomPill.child(
                UIComponents.label(Component.literal("  ●"))
                        .color(Color.ofRgb(0xF1C40F))
                        .shadow(true)
        );

        rootComponent.child(bottomPill);
    }

    private void createRoleCard(
            FlowLayout container,
            Role role,
            ItemStack iconItem,
            String title,
            String subtitle,
            int colorHex,
            List<String> pros,
            List<String> cons,
            boolean playerAlreadyChosen
    ) {
        String takenByPlayer = takenRolesMap.get(role);
        boolean isTaken = (takenByPlayer != null);
        String localPlayerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "";
        boolean isTakenByMe = isTaken && takenByPlayer.equalsIgnoreCase(localPlayerName);

        // Outer Card Container (strictly uniform: width 200px, height 255px)
        FlowLayout card = UIContainers.verticalFlow(Sizing.fixed(200), Sizing.fixed(255));

        // Neon border and dark glass background
        int borderColor = isTaken ? 0xFF2D3436 : colorHex;
        card.surface(Surface.flat(0xDD121124).and(Surface.outline(borderColor)));
        card.padding(Insets.of(6));
        card.margins(Insets.horizontal(4));
        card.horizontalAlignment(HorizontalAlignment.CENTER);

        // 1. Icon Box at the top (with subtle inner border)
        FlowLayout iconBox = UIContainers.verticalFlow(Sizing.fixed(184), Sizing.fixed(38));
        iconBox.surface(Surface.flat(0x991B1A30).and(Surface.outline(0xFF2E2D48)));
        iconBox.horizontalAlignment(HorizontalAlignment.CENTER);
        iconBox.verticalAlignment(VerticalAlignment.CENTER);

        var itemComponent = UIComponents.item(iconItem);
        iconBox.child(itemComponent);
        card.child(iconBox);

        // 2. Title and Subtitle
        card.child(
                UIComponents.label(Component.literal("§l" + title))
                        .color(Color.ofRgb(colorHex))
                        .shadow(true)
                        .margins(Insets.top(3))
        );

        card.child(
                UIComponents.label(Component.literal("§8" + subtitle))
                        .shadow(true)
                        .margins(Insets.bottom(4))
        );

        // 3. Pros and Cons list (fixed height 98px so all cards align perfectly, with automatic text wrap)
        FlowLayout perksLayout = UIContainers.verticalFlow(Sizing.fill(100), Sizing.fixed(98));
        perksLayout.horizontalAlignment(HorizontalAlignment.LEFT);
        perksLayout.verticalAlignment(VerticalAlignment.TOP);
        perksLayout.margins(Insets.bottom(6));

        for (String pro : pros) {
            var label = UIComponents.label(Component.literal("§a" + pro))
                    .shadow(true)
                    .maxWidth(184)
                    .margins(Insets.vertical(1));
            perksLayout.child(label);
        }

        for (String con : cons) {
            var label = UIComponents.label(Component.literal("§c" + con))
                    .shadow(true)
                    .maxWidth(184)
                    .margins(Insets.vertical(1));
            perksLayout.child(label);
        }
        card.child(perksLayout);

        // 4. Action Button / Slot
        if (isTaken) {
            String label = isTakenByMe ? "✓ " + takenByPlayer + " (Вы)" : "✗ " + takenByPlayer;
            int badgeColor = isTakenByMe ? 0xFF27AE60 : 0xFF34495E;

            FlowLayout badge = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            badge.surface(Surface.flat(0xDD000000).and(Surface.outline(badgeColor)));
            badge.horizontalAlignment(HorizontalAlignment.CENTER);
            badge.verticalAlignment(VerticalAlignment.CENTER);
            badge.child(UIComponents.label(Component.literal((isTakenByMe ? "§a" : "§7") + label)).shadow(true));
            card.child(badge);
        } else {
            // Button: Select role
            var btn = UIComponents.button(Component.literal(playerAlreadyChosen ? "Заблокировано" : "ВЫБРАТЬ"), button -> {
                if (!playerAlreadyChosen) {
                    ClientPlayNetworking.send(new SelectRolePayload(role.name()));
                    this.onClose();
                }
            });
            btn.sizing(Sizing.fill(100), Sizing.fixed(20));
            if (playerAlreadyChosen) {
                btn.active = false;
            }
            card.child(btn);
        }

        container.child(card);
    }
}
