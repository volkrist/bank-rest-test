# Bank Cards REST API

## Описание

Этот проект — REST API для управления банковскими картами и переводами между ними.

Реализована:

- аутентификация по JWT,
- роли пользователей (USER / ADMIN),
- безопасное хранение номеров карт (шифрование),
- атомарные переводы средств между картами,
- валидации и бизнес-правила.

Проект сделан как backend-MVP, максимально приближённый к реальной банковской логике.

## Стек технологий

- Java 17
- Spring Boot
- Spring Security (JWT)
- PostgreSQL
- Docker
- Hibernate / JPA
- OpenAPI / Swagger

## Основные возможности

### Аутентификация

- Логин по username/password
- JWT токен
- Роли: USER и ADMIN
- Защищённые эндпоинты

### Карты

- Создание карты
- Получение списка карт
- Получение карты по id
- Маскирование номера карты при выдаче
- Номер карты хранится только в зашифрованном виде
- Статусы карты (ACTIVE / BLOCKED / EXPIRED)

### Переводы

- Перевод средств между своими картами
- Атомарность операций (transaction)
- Проверки:
  - недостаточно средств
  - перевод на ту же карту
  - заблокированная карта

### Безопасность

- JWT Bearer Authentication
- Role-based access control
- ADMIN-only операции:
  - удаление карты
  - создание пользователей
- Номер карты:
  - не хранится в открытом виде
  - в БД хранится encrypted_number + last_four

## API документация (Swagger)

После запуска приложения Swagger доступен по адресу:

**http://localhost:8080/swagger-ui/index.html**

Поддерживается кнопка **Authorize** (JWT Bearer).

## Запуск проекта

### 1. Запуск PostgreSQL

```bash
docker compose up -d
```

### 2. Запуск приложения

```bash
./mvnw spring-boot:run
```

На Windows (PowerShell/CMD):

```cmd
.\mvnw.cmd spring-boot:run
```

### Запуск тестов

```bash
.\mvnw.cmd test
```

Интеграционные тесты (TransferServiceIT) используют Testcontainers и требуют **запущенный Docker**. Если ключ шифрования задаётся только через переменную окружения:

```powershell
$env:CARD_ENCRYPTION_KEY="AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
.\mvnw.cmd test
```

### 3. Тестовые пользователи (dev-профиль)

| username | password | role  |
|----------|----------|-------|
| admin    | password | ADMIN |
| user     | password | USER  |

## Примеры запросов

### Логин

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### Создание карты

```http
POST /api/cards
Content-Type: application/json
Authorization: Bearer <token>

{
  "number": "4111111111111111",
  "holderName": "TEST USER",
  "expiryDate": "2030-12-31"
}
```

(Баланс новой карты — 0; пополнение отдельно не реализовано в MVP.)

### Перевод средств

```http
POST /api/transfers
Content-Type: application/json
Authorization: Bearer <token>

{
  "fromCardId": 1,
  "toCardId": 2,
  "amountCents": 100
}
```

## Тестирование

Проект был протестирован вручную через Swagger и PostgreSQL.

Покрыто:

- аутентификация и JWT
- разграничение ролей USER / ADMIN
- CRUD операций с картами
- бизнес-валидации переводов
- проверка атомарности транзакций
- хранение номера карты в зашифрованном виде

Полный список тест-кейсов см. в файле [TEST_CASES.md](TEST_CASES.md).

## Статус проекта

Проект завершён на уровне backend MVP и готов к расширению:

- audit log,
- лимиты,
- idempotency,
- integration tests.

## Что демонстрирует проект

- проектирование REST API
- JWT-аутентификацию и роли
- работу с транзакциями
- шифрование чувствительных данных
- валидацию бизнес-правил
- работу с PostgreSQL и Docker
