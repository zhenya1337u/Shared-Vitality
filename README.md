# 🩸 Shared Vitality

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-26.2%20(Fabric)-6E8B3D?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft 26.2 Fabric" />
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Status-Active-4CAF50?style=for-the-badge" alt="Status Active" />
  <img src="https://img.shields.io/badge/License-CC0%201.0-000000?style=for-the-badge" alt="License CC0 1.0" />
</p>

<p align="center">
  <strong>Кровная связь</strong> — командная выживалка, где весь отряд живёт одним телом.
</p>

<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=24&duration=2500&pause=800&color=E74C3C&center=true&vCenter=true&width=900&lines=Shared+Vitality;Один+пул+здоровья;Одна+связь;Одна+победа" alt="Shared Vitality banner" />
</p>

---

## 🌌 Что это такое

Shared Vitality — это мод для Minecraft 26.2 на Fabric, в котором команда не играет как набор независимых игроков, а как единый организм.

Каждый участник команды связан общим запасом здоровья, общей системой дистанции и уникальной ролью. Это делает игру более напряжённой, стратегической и совместной.

> Ты не выживаешь один. Ты выживаешь вместе.

---

## ✨ Главные механики

### 🩺 Общий пул здоровья

У команды есть один общий запас «кровной жизненной силы».

- любой урон по одному союзнику влияет на общий пул
- система защищена от зацикливания урона
- если пул падает до нуля — команда сталкивается с критическим исходом

### 🔗 Связь между игроками

Механика tether задаёт физическую и стратегическую зависимость участников.

- до 64 блоков — стабильная связь
- 64–128 блоков — натяжение, ухудшение состояния
- свыше 128 блоков — серьёзные дебаффы
- разные измерения усиливают нагрузку и повышают риск потери связи

### 🧠 Роли и классы

В начале партии игроки получают роль, которая влияет на стиль игры и баланс команды.

Список ролей включает:

- 🛡 Guardian
- 🧪 Apothecary
- ⛏ Delver
- 🏹 Scout
- ⚔ Reaper
- 📖 Enchanter
- 🧱 Mason
- 🐺 Beastmaster
- 🗡 Shadow
- 👻 Soulbinder

Каждая роль даёт преимущества, но и накладывает ограничения. Команда создаётся не просто по навыкам, а по синергии ролей.

### 🩹 Кровавый бинт

Специальный предмет помогает восстанавливать общий запас жизненной силы. Он поддерживает идею «командного выживания» вместо изолированного одиночного выживания.

---

## 🎯 Цель проекта

Shared Vitality создаёт ощущение, что каждый игрок — часть одной живой системы:

- стратегия строится вокруг коммуникации
- позиционирование критично
- роли влияют на победу
- выживание становится командным опытом, а не набором отдельных событий

---

## 🛠 Технологии

- Minecraft: 26.2
- Loader: Fabric
- Язык: Java 25
- Сборка: Gradle + Fabric Loom
- Зависимости: Fabric API, owo-lib и дополнительные модули по необходимости

---

## 🚀 Установка

1. Скачайте актуальный релиз на странице Releases.
2. Переместите .jar в папку `mods` вашего Fabric-клиента.
3. Запустите игру.
4. Подключитесь к миру с включённым модом.

Ссылки:
- Репозиторий: https://github.com/zhenya1337u/Shared-Vitality
- Releases: https://github.com/zhenya1337u/Shared-Vitality/releases

---

## 🏗 Сборка из исходников

```bash
git clone https://github.com/zhenya1337u/Shared-Vitality.git
cd Shared-Vitality
./gradlew build
```

После сборки готовый jar будет лежать в папке `build/libs`.

---

## 📁 Структура проекта

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
├── gradlew.bat
├── LICENSE
├── README.md
├── idea.md
└── .gitignore
```

---

## ⚙️ Конфигурация

В проекте предусмотрены параметры, которые можно использовать для балансировки:

- `pool_scaling_per_player`
- `tether_distance_warning`
- `cross_dimension_decay`
- `friendly_fire`

Подробности находятся в логике конфигов и в файле `idea.md`.

---

## 🧩 Почему это интересно

Потому что это не просто набор умений, а целостная идея:

- каждый игрок является частью одной команды
- значит, выживание зависит от координации
- риски и награды распределяются по группе
- игра становится более драматичной и живой

---

## 🤝 Вклад в проект

Вклад приветствуется.

1. Сделай форк репозитория
2. Создай ветку с фичей
3. Внеси изменения
4. Открой Pull Request

---

## 📜 Лицензия

Проект распространяется под лицензией Creative Commons Zero v1.0 Universal (CC0 1.0).

Подробности — в файле `LICENSE`.

---

<p align="center">
  <sub>Made for brave squads and shared survival.</sub>
</p>
