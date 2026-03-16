# Tmatch

`Tmatch` это Android-приложение, построенное на Kotlin, Jetpack Compose, Koin, Ktor и Firebase Cloud Messaging.

## Структура проекта

- `app` — точка входа приложения и сборка зависимостей.
- `api/*` — контракты домена, навигации и интеграционных gateway.
- `feature/*` — реализация пользовательских фич.
- `data/tmatch_api` — работа с backend API и локальным хранением, включая регистрацию FCM device token.
- `common/ui` — общие UI-компоненты и тема.

## Интеграции

- Firebase Cloud Messaging используется для получения push-сообщений.
- FCM device token регистрируется на backend через `POST /notifications/devices`.
- Входящие push payload на текущем этапе только логируются в `logcat`.

## Архитектура

Описание принятых архитектурных решений находится в [ADR.md](./ADR.md).
