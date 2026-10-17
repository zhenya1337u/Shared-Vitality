```markdown
name=README.md

# 🩸 Shared Vitality — «Кровная связь» (мод для Minecraft)

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-26.2%20%28Fabric%29-6E8B3D?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft 26.2 Fabric" />
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Лицензия-CC0%201.0-000000?style=for-the-badge" alt="License CC0 1.0" />
  <img src="https://img.shields.io/badge/Статус-Активен-4CAF50?style=for-the-badge" alt="Active" />
</p>

<p align="center">⚔️ Командный мод для энергичной и рискованной совместной выживалки — все игроки связаны одним общим пулом здоровья.</p>

---

## 🎮 Кратко

Shared Vitality (Кровная связь) — мод для Fabric (Minecraft 26.2), который превращает командную игры в единое целое: у команды один общий пул здоровья, система растяжения связи (tether), роли с уникальными способностями и предметы, влияющие на общий пул.

Идея: сделать кооперативную игру более стратегичной и зависимой от слаженных действий команды.

---

## ✨ Основные механики

- 🩺 Единый пул здоровья (Bloodlink Pool)
  - Урон, полученный любым союзником, отнимается из общего пула.
  - Защита от зацикливания урона (no damage loops).
  - Если пул = 0 — возможна групповая смерть (настраивается).

- 🔗 Тетивная система (Tether)
  - До 64 блоков — нормальное поведение.
  - 64–128 блоков — предупреждения и ослабление эффектов.
  - >128 блоков или разные измерения — дебаффы: голод/слабость и постепенное сжигание HP общего пула.

- 🧭 Роли (10 типов)
  - Guardian, Apothecary, Delver, Scout, Reaper, Enchanter, Mason, Beastmaster, Shadow, Soulbinder
  - Каждая роль имеет плюсы и минусы и влияет на общую стратегию команды.

- 🩹 Крафтовый предмет «Кровавый бинт» — восстанавливает здоровье в общий пул и поощряет совместное использование ресурсов.

---

## 🚀 Быстрый старт (для игроков)

1. Скачайте последнюю версию из релизов: https://github.com/zhenya1337u/Shared-Vitality/releases
2. Поместите .jar мод в папку `mods` вашего Fabric-инстанса.
3. Запустите Minecraft с Fabric Loader (версия, совместимая с 26.2).
4. Заходите в мир и используйте команды/GUI (см. раздел «Команды» в будущем).

---

## 🛠️ Сборка из исходников (для разработчиков)

Проект использует Gradle (Fabric Loom).

```bash
git clone https://github.com/zhenya1337u/Shared-Vitality.git
cd Shared-Vitality
./gradlew build
```

Сборка создаст jar в `build/libs/`. Для локального тестирования есть задача `runClient` (если настроено окружение Fabric).

---

## 🧭 Структура проекта

```text
Shared-Vitality/
├── src/
│   ├── client/
│   └── main/
│       ├── java/net/sharedvitality/
│       │   ├── compat/
│       │   ├── config/
│       │   ├── item/
│       │   ├── network/
│       │   ├── pool/
│       │   ├── role/
│       │   └── SharedVitalityMod.java
│       └── resources/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── LICENSE
└── idea.md
```

---

## ⚙️ Конфигурация

Основные опции конфигурации (пример в `config.json5`):

- `pool_scaling_per_player` — базовый HP на игрока (по умолчанию `20.0`).
- `tether_distance_warning` — границы тетива (по умолчанию `64.0`).
- `cross_dimension_decay` — включать ли урон при разных измерениях.
- `friendly_fire` — учитывается ли дружелюбный урон в общий пул.

(Точные ключи и пути см. в пакете `net.sharedvitality.config` и в `idea.md`.)

---

## 📷 Скриншоты и баннеры

Добавь картинки в `assets/screenshots/` или `docs/` и подключи их сюда в README. Пример:

```markdown
![Игровой скриншот](assets/screenshots/screenshot1.png)
```

Если хочешь — пришли мне изображения, и я вставлю их в README (или могу добавить заглушки/баннер из шаблона).

---

## 🧩 Совместимость и зависимости

- Fabric Loader — см. `gradle.properties` (loader_version)
- Fabric API — требуется
- owo-lib — в зависимостях (смотри `build.gradle`)
- Опционально: Balm, Hardcore Revival (compileOnly)

---

## 🤝 Вклад и контакты

Вклад приветствуется:

1. Форкни репозиторий.
2. Создай ветку `feature/описание`.
3. Сделай изменения и добавь тесты, если нужно.
4. Открой Pull Request с описанием.

Контакты: [@zhenya1337u](https://github.com/zhenya1337u)

---

## 📜 Лицензия

Проект распространяется под Creative Commons Zero v1.0 Universal (CC0 1.0). Подробности в файле LICENSE.

---

<p align="center">💀 Сделаем совместную выживалку опасной, честной и эпичной. Если хочешь — добавлю баннер, больше эмодзи и полностью «игровой» стиль README.</p>
```
