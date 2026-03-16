# Tmatch: описание функционала и статуса интеграции

Дата подготовки: 2026-03-17

## Назначение документа

Документ фиксирует:

- текущий состав функционала приложения
- границы первой версии
- статус завершённости пользовательских сценариев
- список backend API, реально подключённых в мобильном клиенте
- известные ограничения текущей реализации

## Ключевая ценность продукта

`Tmatch` решает базовый сценарий знакомств внутри одного мобильного приложения:

1. Пользователь создаёт аккаунт и профиль
2. Заполняет dating-профиль
3. Получает входящие лайки и рекомендации
4. Совершает действия над карточками
5. Переходит в профиль кандидата и при наличии чата продолжает общение в сообщениях

Основной пользовательский маршрут следует показывать именно в этой последовательности, без смешения с дополнительными диагностическими и отладочными возможностями.

## Границы первой версии

### Входит в первую версию

- Email sign-in / sign-up
- Создание и редактирование базового профиля
- Создание и редактирование dating profile
- Matching feed: incoming likes + recommendations
- Действия над карточками: like / dislike / block
- Список чатов и экран чата
- Загрузка истории сообщений
- Отправка сообщений
- Realtime обновление чата через websocket
- FCM token registration
- Debug panel с логами и API overrides
- Базовая обработка offline / unauthorized / validation / service errors

### Дополнительный функционал поверх первой версии

- Локальный кэш чатов и сообщений через Room
- Optimistic UI и очередь pending actions для matching
- Стартовый resolver, который восстанавливает сценарий после перезапуска
- Debug-shake для входа в панель отладки
- FCM системные уведомления

### Вне текущего объёма или реализовано частично

- Нет unread counters и read-status для чатов
- Нет подтверждённого deep-link/open-chat поведения по push payload
- Нет неблокирующей миграции Room: сейчас используется destructive fallback т.к. room необходим для кэширования и не хранит важных данных
- Нет полной гарантии background-resend для pending actions через Worker/JobScheduler

## Архитектурный контур

Проект реализован как модульное Android-приложение:

- `app` — composition root, старт приложения, глобальная навигация, debug, FCM
- `api/*` — контракты домена, gateway-интерфейсы, navigation keys
- `feature/*` — экраны, ViewModel, use case
- `data/tmatch_api` — Ktor API client, Room, session storage, websocket manager
- `common/ui` — общие UI-компоненты, тема, no-internet экран

Практический вывод: приложение представляет собой законченный клиентский контур с разделением контрактов, UI и инфраструктуры.

## Матрица функционала и API

| Блок | Пользовательский сценарий | Экран / модуль | Backend API | Статус | Что гарантированно подключено |
| --- | --- | --- | --- | --- | --- |
| Auth | Вход по email | `feature/auth` | `POST /auth/signing/email` | Реализовано | Отправка credentials, получение `accessToken` и `userId` |
| Auth | Регистрация по email | `feature/auth` | `POST /auth/registration/email` | Реализовано | Создание аккаунта и получение сессии |
| Auth/Profile | Получение текущего профиля | `feature/auth` | `GET /profile` | Реализовано | Проверка существования account profile |
| Auth/Profile | Создание/редактирование account profile | `feature/auth` | `PUT /profile` | Реализовано | Сохранение имени, даты рождения, пола, региона, avatar URL |
| Preview | Preview cards для onboarding/jury preview | `feature/auth` | `GET /preview/cards` | Реализовано | Получение списка preview-карточек |
| Profile | Определение стартового маршрута | `app` + `feature/profile` | `GET /profile/card` | Реализовано | Выбор между onboarding / profile setup / profile / matching |
| Profile | Просмотр собственной карточки состояния | `feature/profile` | `GET /profile/card` | Реализовано | Проверка наличия account/dating profile |
| Profile | Просмотр чужой карточки | `feature/profile` | `GET /profile/card/{userId}` | Реализовано | Открытие профиля пользователя из matching/chats |
| Dating Profile | Получение своего dating profile | `feature/profile` | `GET /dating-profile/me` | Реализовано | Загрузка dating-фото и traits |
| Dating Profile | Сохранение dating profile | `feature/profile` | `PUT /dating-profile/me` | Реализовано | Обновление photo list |
| Dating Profile | Загрузка фото dating profile | `feature/auth`, `feature/profile` | `POST /dating-profile/me/photos` | Реализовано | Upload binary photo и получение `photoUrl` |
| Matching | Получение incoming likes | `feature/matching` | `GET /likes/incoming/me` | Реализовано | Получение кандидатов и explainability reasons |
| Matching | Получение recommendations | `feature/matching` | `GET /recommendations` | Реализовано | Получение карточек рекомендаций |
| Matching | Получение targeting preferences | `feature/matching` | `GET /targeting/me` | Реализовано | Подтягиваются текущие настройки подбора |
| Matching | Сохранение targeting preferences | `feature/matching` | `PUT /targeting/me` | Реализовано | Сохранение региона, пола, возрастного диапазона |
| Matching | Действие над карточкой | `feature/matching` | `POST /interactions` | Реализовано | Отправка `like` / `dislike` / `block`, поддержка очереди при нестабильной сети |
| Chats | Получение списка чатов | `feature/chats` | `GET /chats/me` | Реализовано | Список чатов с `other_user_card` и last message |
| Chats | Получение истории сообщений | `feature/chats` | `GET /chats/{chatId}/messages` | Реализовано | Пагинационная догрузка истории |
| Chats | Отправка сообщения | `feature/chats` | `POST /chats/{chatId}/messages` | Реализовано | Отправка текстового сообщения |
| Chats | Realtime входящие сообщения | `feature/chats` | `WS /chat/stream?token=...` | Реализовано | Обновление локальной БД и UI через websocket event |
| Notifications | Регистрация устройства для push | `app` | `POST /notifications/devices` | Реализовано | Device token отправляется при наличии авторизации |

## Подтверждение завершённости по сценариям

| Сценарий | Статус | Комментарий |
| --- | --- | --- |
| Авторизация -> создание профиля | Реализовано | Есть экранный flow, API и стартовая маршрутизация после перезапуска |
| Авторизация -> dating profile -> matching | Реализовано | Стартовый resolver отправляет пользователя в matching только при наличии полного набора данных |
| Matching -> просмотр профиля кандидата | Реализовано | Из matching можно открыть user profile |
| Matching -> действие -> продолжение колоды | Реализовано | Используется optimistic UI, колода не блокируется ожиданием ответа сервера |
| Список чатов -> чат -> отправка сообщения | Реализовано | Есть список, история, отправка, обновление last message |
| Чат -> входящее realtime сообщение | Реализовано | Приходит через websocket и кладётся в Room |
| Offline recovery | Реализовано частично | Есть единый no-internet flow и retry, но нет полноценного background-sync через worker |
| Push notifications | Реализовано частично | Регистрация token и показ системного уведомления есть, но серверные payload-сценарии требуют отдельного ручного прогона |
| Отладка проблем с backend | Реализовано | Есть debug logs и подмена API-ответов JSON-ами |

## Сильные стороны текущей реализации

### 1. Основной сценарий собран end-to-end

От email auth до использования matching и чатов есть связный маршрут, а не набор отдельных экранов.

### 2. Подключение к API прозрачно

По коду явно видны gateway-классы и конкретные endpoints. Можно точно отделить клиентскую логику, сетевые контракты и локальное хранение.

### 3. Реализация учитывает мобильный контекст

- единая обработка `NoInternet`
- очистка сессии на `401`
- локальный кэш чатов через Room
- websocket для realtime-сценария
- очередь pending actions для нестабильной сети
- debug overrides для тестирования поведения при нестабильном backend

## Известные ограничения

- В репозитории нет backend-сервиса, поэтому документ подтверждает клиентские контракты и интеграцию, но не серверную реализацию
- Чаты не поддерживают unread counters и read receipts, потому что текущий контракт этого не даёт
- Room настроен через `fallbackToDestructiveMigration`, поэтому локальный кэш чатов пока не гарантирует безболезненную эволюцию схемы между версиями
- Realtime-сценарий покрывает входящие сообщения, но при падении websocket приложение деградирует к HTTP refresh, а не к fully managed reconnect policy
- Push-путь требует ручного e2e-прогона с реальной доставкой сообщения

## Технические подтверждения в коде

- DI и composition root: `app/src/main/java/com/prod/t_match/TMatchApplication.kt`
- Глобальная навигация и debug-panel: `app/src/main/java/com/prod/t_match/TMatchApp.kt`
- Стартовый resolver: `app/src/main/java/com/prod/t_match/navigation/AppStartDestinationResolver.kt`
- Auth API: `data/tmatch_api/src/main/java/com/prod/tmatch_api/auth/data/gateway/KtorAuthGateway.kt`
- Matching API: `data/tmatch_api/src/main/java/com/prod/tmatch_api/match/data/gateway/KtorMatchGateway.kt`
- Profile API: `data/tmatch_api/src/main/java/com/prod/tmatch_api/profile/data/gateway/KtorProfileGateway.kt`
- Chats API + Room cache: `data/tmatch_api/src/main/java/com/prod/tmatch_api/chats/data/gateway/KtorChatGateway.kt`
- Chat websocket: `data/tmatch_api/src/main/java/com/prod/tmatch_api/chats/data/gateway/KtorChatStreamManager.kt`
- Ошибки API: `data/tmatch_api/src/main/java/com/prod/tmatch_api/common/data/network/KtorGatewaySupport.kt`
- FCM token registration: `app/src/main/java/com/prod/t_match/notifications/FcmTokenRegistrar.kt`
- Push notifications: `app/src/main/java/com/prod/t_match/notifications/TmatchFirebaseMessagingService.kt`
- Debug overrides: `app/src/main/java/com/prod/t_match/debug/DebugNetworkOverridesScreen.kt`
- Debug logs: `app/src/main/java/com/prod/t_match/debug/DebugAppLogsScreen.kt`

## Автоматическая верификация

На 2026-03-17 успешно выполнен:

```bash
./gradlew test
```

Это подтверждает, что проект собирается и проходит набор unit-тестов в текущем состоянии репозитория.
