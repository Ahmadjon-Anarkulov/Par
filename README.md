# Parlament Bot

Telegram-бот на Java 21 + Spring Boot 3, задеплоенный на Railway.app.

## Быстрый старт

### Локально

```bash
export BOT_TOKEN="токен_от_botfather"
export BOT_USERNAME="username_бота_без_собаки"

mvn clean package -DskipTests
java -jar target/parlament-bot-1.0.0.jar
```

Проверка: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

### Деплой на Railway

1. Залейте репозиторий на GitHub
2. Зайдите на [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Выберите репозиторий — Railway сам найдёт `Dockerfile` и `railway.toml`
4. Во вкладке **Variables** добавьте:

| Переменная | Пример |
|---|---|
| `BOT_TOKEN` | `123456789:AAFxxxxxx` |
| `BOT_USERNAME` | `MyParlamentBot` |
| `BOT_MODE` | `long_polling` |

5. Нажмите **Deploy** — готово.

## Все переменные окружения

| Переменная | Обязательная | Описание |
|---|---|---|
| `BOT_TOKEN` | ✅ | Токен от @BotFather |
| `BOT_USERNAME` | ✅ | Username бота без `@` |
| `BOT_MODE` | ❌ | `long_polling` (по умолчанию) или `webhook` |
| `BOT_WEBHOOK_ENABLED` | ❌ | `true` для webhook режима |
| `BOT_WEBHOOK_PUBLIC_URL` | ❌* | URL Railway сервиса, напр. `https://xxx.up.railway.app` |
| `BOT_WEBHOOK_PATH` | ❌ | `/telegram/webhook` (по умолчанию) |
| `PORT` | ❌ | Railway подставляет автоматически |

> `BOT_WEBHOOK_PUBLIC_URL` обязателен если `BOT_MODE=webhook`

## Структура

```
src/main/java/com/parlament/
├── ParlamentBotApplication.java   ← точка входа
├── config/         ← BotProperties, TelegramConfig
├── telegram/       ← Long polling / Webhook бот
├── handler/        ← Command, Text, Callback обработчики
├── service/        ← Cart, Order, Session
├── model/          ← Product, Order, CartItem, UserSession
├── data/           ← ProductCatalog (in-memory)
├── repository/     ← InMemoryProductRepository
├── controller/     ← Webhook endpoint
└── util/           ← KeyboardFactory, MessageFormatter
```
