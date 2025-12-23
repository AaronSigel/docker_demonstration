# Демонстрационное приложение на микросервисной архитектуре

## Описание архитектуры

Проект представляет собой демонстрационный стенд микросервисной архитектуры с CI/CD и мониторингом.

### Микросервисы

1. **workspace-service** (порт 8081)
   - Управление рабочими пространствами
   - Эндпоинты: `GET /api/workspaces`, `POST /api/workspaces`

2. **booking-service** (порт 8082)
   - Управление бронированиями
   - Эндпоинты: `POST /api/bookings`, `GET /api/bookings/{id}`

3. **payment-service** (порт 8083)
   - Имитация обработки платежей
   - Эндпоинты: `POST /api/payments`, `GET /api/payments/{id}`

### Инфраструктура

- **Jenkins** (порт 8080) - CI/CD pipeline
- **Prometheus** (порт 9090) - сбор метрик
- **Grafana** (порт 3000) - визуализация метрик

## Быстрый старт

### Запуск стенда

```bash
docker compose up -d --build
```

Ожидание полного запуска всех сервисов (около 1-2 минут):

```bash
docker compose ps
```

### URL сервисов

- **Workspace Service**: http://localhost:8081
- **Booking Service**: http://localhost:8082
- **Payment Service**: http://localhost:8083
- **Jenkins**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### Проверка работоспособности

Проверка health всех сервисов:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

Проверка версий:

```bash
curl http://localhost:8081/api/version
curl http://localhost:8082/api/version
curl http://localhost:8083/api/version
```

## Автоматический деплой через Jenkins

### Обзор настройки

Проект настроен для автоматического деплоя через Jenkins Pipeline с использованием **polling SCM** (без webhook):

- **Автозапуск сборки**: Jenkins проверяет репозиторий каждую минуту на наличие новых коммитов
- **Автоматический деплой**: При успешной сборке выполняется деплой app-сервисов через `docker compose`
- **Smoke-тесты**: После деплоя автоматически проверяются health и version endpoints всех сервисов
- **Изоляция Jenkins**: Jenkins не перезапускается при деплое (деплойятся только app-сервисы)

### Архитектура Pipeline

Pipeline состоит из следующих stages:

1. **Checkout** - получение кода из репозитория
2. **Preflight** - проверка доступности Docker и Docker Compose
3. **Build** - сборка Docker образов для app-сервисов (`workspace-service`, `booking-service`, `payment-service`)
4. **Deploy** - поднятие/обновление контейнеров через `docker compose up -d`
5. **Smoke Tests** - проверка доступности сервисов через Docker сеть

После завершения pipeline (в блоке `post`) выводятся:
- Статус всех контейнеров
- Последние 200 строк логов каждого app-сервиса

## Настройка Jenkins

### Подготовка к запуску

Jenkins настроен для запуска от root (для демо-окружения), что обеспечивает доступ к Docker socket без дополнительных настроек прав.

**Запуск Jenkins:**

```bash
# Простой запуск (DOCKER_GID не требуется, т.к. Jenkins запускается от root)
docker compose up -d --build jenkins
```

**Примечание:** В продакшене рекомендуется использовать запуск от пользователя `jenkins` с правильной настройкой `group_add` и `DOCKER_GID`. Для демо-окружения запуск от root упрощает настройку.

**Если нужно использовать запуск от пользователя jenkins (альтернативный вариант):**

```bash
# Получить GID группы Docker
export DOCKER_GID=$(stat -c %g /var/run/docker.sock)
echo "DOCKER_GID=$DOCKER_GID"

# Запустить Jenkins с указанием GID
DOCKER_GID=$DOCKER_GID docker compose up -d --build jenkins
```

Или установить переменную окружения в `.env` файл:

```bash
echo "DOCKER_GID=$(stat -c %g /var/run/docker.sock)" > .env
docker compose up -d --build jenkins
```

**Важно:** Если Jenkins уже запущен и вы обновили `fix-permissions.sh`, необходимо пересобрать контейнер:

```bash
docker compose stop jenkins
docker compose build jenkins
docker compose up -d jenkins
```

### Первоначальная настройка Jenkins

1. Откройте http://localhost:8080
2. Получите начальный пароль администратора:

```bash
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

3. Установите рекомендуемые плагины
4. Создайте администратора
5. Установите необходимые плагины (Manage Jenkins → Plugins → Available):
   - **Pipeline** (обычно уже установлен)
   - **Git** (для работы с Git репозиториями)
   - **GitHub Branch Source** (для Multibranch Pipeline и webhook)

### Настройка Credentials (для private репозиториев)

Если репозиторий приватный, необходимо создать GitHub Personal Access Token (PAT):

1. В GitHub: Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Создайте токен с правами `repo` (для private репо)
3. В Jenkins: Manage Jenkins → Credentials → System → Global credentials → Add Credentials
   - Kind: `Secret text`
   - Secret: вставьте ваш PAT
   - ID: `github-pat` (или другое имя)
   - Description: `GitHub Personal Access Token`

### Создание Pipeline Job с автоматическим polling SCM

Настройка Pipeline job для автоматического обнаружения новых коммитов через polling (без webhook):

1. **Создание Pipeline job:**
   - New Item → Pipeline
   - Название: `microservices-pipeline` (или любое другое)
   - Нажмите OK

2. **Настройка Pipeline:**
   - В разделе "Pipeline":
     - Definition: `Pipeline script from SCM`
     - SCM: `Git`
     - Repositories:
       - Repository URL: URL вашего GitHub репозитория (например, `https://github.com/username/repo.git`)
       - Credentials: выберите созданный PAT (для private репо) или оставьте пустым (для public)
     - Branches to build:
       - Branch Specifier: `*/main` (или `*/master`, в зависимости от вашей основной ветки)
     - Script Path: `Jenkinsfile`

3. **Важно: НЕ включайте Poll SCM в UI!**
   - В разделе "Build Triggers" **НЕ** ставьте галочку "Poll SCM"
   - Polling настроен непосредственно в `Jenkinsfile` через `triggers { pollSCM('H/1 * * * *') }`
   - Это предотвращает конфликт между UI и Jenkinsfile

4. Сохраните настройки (Save)

### Проверка работы Polling SCM

После создания job Jenkins начнёт автоматически проверять репозиторий каждую минуту на наличие новых коммитов.

**Проверка Polling Log:**

1. Откройте созданный job в Jenkins
2. Перейдите в "Polling Log" (ссылка в левом меню)
3. Вы увидите историю проверок репозитория:
   - Если есть новые коммиты → сборка запустится автоматически
   - Если изменений нет → сборка не запустится

**Пример вывода Polling Log:**
```
Started on [дата и время]
Polling [URL репозитория]
Done. Took [время] ms. No changes
```

или

```
Started on [дата и время]
Polling [URL репозитория]
Changes found
```

**Ручной запуск сборки:**

Для проверки pipeline без ожидания polling:
- Откройте job → "Build Now"

### Проверка доступа Jenkins к Docker

После настройки проверьте, что Jenkins может выполнять Docker команды:

1. В Jenkins: Manage Jenkins → Script Console
2. Выполните:

```groovy
sh 'docker ps'
```

Если команда выполняется без ошибок `permission denied`, доступ настроен корректно.

## Демонстрационный сценарий

### 1. Поднятие стенда

```bash
docker compose up -d --build
```

### 2. Генерация трафика

Запустите скрипт генерации трафика:

```bash
./generate-traffic.sh
```

Или вручную:

```bash
# Создание workspace
curl -X POST http://localhost:8081/api/workspaces -H "Content-Type: application/json" -d '{"name":"Test Workspace"}'

# Создание booking
curl -X POST http://localhost:8082/api/bookings -H "Content-Type: application/json" -d '{"workspaceId":1,"userId":"user123"}'

# Создание payment
curl -X POST http://localhost:8083/api/payments -H "Content-Type: application/json" -d '{"bookingId":1,"amount":100.50}'
```

### 3. Проверка автоматического деплоя через polling SCM

#### 3.1 Тестирование автоматического запуска сборки

1. **Внесите изменения в репозиторий:**
   - Измените версию в одном из сервисов (например, в `services/workspace-service/src/main/resources/application.properties`):
     ```properties
     app.version=1.0.1
     ```

2. **Закоммитьте и отправьте изменения:**
   ```bash
   git add .
   git commit -m "Update version to 1.0.1"
   git push origin main
   ```

3. **Ожидание автоматического запуска:**
   - Jenkins автоматически обнаружит новый коммит в течение ≤ 1 минуты (polling каждую минуту)
   - Откройте http://localhost:8080 → выберите job `microservices-pipeline`
   - В списке сборок появится новая сборка (статус: "In progress" или "Building")

4. **Мониторинг выполнения pipeline:**
   - Откройте запущенную сборку
   - Pipeline выполнит следующие stages:
     - **Checkout**: получение кода из репозитория
     - **Preflight**: проверка доступности Docker и Docker Compose
     - **Build**: сборка Docker образов для app-сервисов
     - **Deploy**: поднятие/обновление контейнеров через `docker compose up -d`
     - **Smoke Tests**: проверка health и version endpoints для каждого сервиса

5. **Проверка результата деплоя:**

   **Статус контейнеров:**
   ```bash
   docker compose -p demo ps
   ```
   Все app-сервисы должны быть в состоянии `Up` (jenkins, prometheus, grafana не должны перезапускаться)

   **Проверка версии сервиса:**
   ```bash
   curl http://localhost:8081/api/version
   ```
   Должна вернуться обновлённая версия `1.0.1`

   **Проверка health endpoints:**
   ```bash
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:8083/actuator/health
   ```

#### 3.2 Проверка Polling Log

Для подтверждения, что polling работает корректно:

1. В Jenkins: откройте job `microservices-pipeline`
2. Нажмите "Polling Log" в левом меню
3. Проверьте последние записи:
   - Должны быть записи каждую минуту
   - После `git push` должна появиться запись "Changes found" и автоматически запуститься сборка

#### 3.3 Проверка логов после деплоя

После успешного завершения pipeline:

1. В Jenkins: откройте завершённую сборку
2. В разделе "Post-build actions" (в конце логов) будут выведены:
   - Статус всех контейнеров (`docker compose ps`)
   - Последние 200 строк логов каждого app-сервиса

Также можно проверить логи напрямую:
```bash
docker compose -p demo logs --tail=50 workspace-service
docker compose -p demo logs --tail=50 booking-service
docker compose -p demo logs --tail=50 payment-service
```

### 4. Просмотр метрик

1. **Prometheus**: http://localhost:9090
   - Проверьте targets: Status → Targets
   - Все сервисы должны быть в статусе UP

2. **Grafana**: http://localhost:3000
   - Логин: `admin`, пароль: `admin`
   - Дашборд "Microservices Metrics" доступен автоматически
   - Метрики: HTTP request rate, error rate, latency

## Структура проекта

```
/
  services/
    workspace-service/     # Микросервис управления workspace
    booking-service/       # Микросервис управления booking
    payment-service/       # Микросервис управления payment
  infra/
    prometheus/
      prometheus.yml       # Конфигурация Prometheus
    grafana/
      provisioning/
        datasources/       # Автоматическая настройка Prometheus datasource
        dashboards/        # Автоматическая загрузка дашбордов
      dashboards/
        microservices.json # Дашборд с метриками
    jenkins/
      Jenkinsfile          # Pipeline для CI/CD
  docker-compose.yml       # Оркестрация всех сервисов
  generate-traffic.sh      # Скрипт генерации трафика
  README.md
```

## API эндпоинты

### Workspace Service (8081)

- `GET /api/workspaces` - список всех workspace
- `POST /api/workspaces` - создание workspace
- `GET /api/version` - версия сервиса
- `GET /actuator/health` - health check
- `GET /actuator/prometheus` - метрики Prometheus

### Booking Service (8082)

- `POST /api/bookings` - создание booking
- `GET /api/bookings/{id}` - получение booking по ID
- `GET /api/version` - версия сервиса
- `GET /actuator/health` - health check
- `GET /actuator/prometheus` - метрики Prometheus

### Payment Service (8083)

- `POST /api/payments` - создание payment
- `GET /api/payments/{id}` - получение payment по ID
- `GET /api/version` - версия сервиса
- `GET /actuator/health` - health check
- `GET /actuator/prometheus` - метрики Prometheus

## Остановка стенда

```bash
docker compose down -v
```

## Troubleshooting

### Просмотр логов

```bash
docker compose logs -f workspace-service
docker compose logs -f booking-service
docker compose logs -f payment-service
docker compose logs -f jenkins
```

### Перезапуск сервиса

```bash
docker compose restart workspace-service
```

### Проверка сети Docker

```bash
docker network ls
docker network inspect microservices_net
```

