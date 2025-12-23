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

## Настройка Jenkins

### Подготовка к запуску

Перед первым запуском Jenkins необходимо получить GID группы Docker для корректного доступа к Docker socket:

```bash
# Получить GID группы Docker
export DOCKER_GID=$(stat -c %g /var/run/docker.sock)
echo "DOCKER_GID=$DOCKER_GID"

# Запустить Jenkins с указанием GID
DOCKER_GID=$DOCKER_GID docker compose up -d --build jenkins
```

Важно: если `DOCKER_GID` не задан, `docker compose` подставит пустое значение и Jenkins может получить `permission denied` при доступе к `/var/run/docker.sock`.

Или установить переменную окружения в `.env` файл:

```bash
echo "DOCKER_GID=$(stat -c %g /var/run/docker.sock)" > .env
docker compose up -d --build jenkins
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

### Создание Multibranch Pipeline

1. New Item → Multibranch Pipeline
2. Название: `microservices-pipeline`
3. Branch Sources → Add source → GitHub
   - Credentials: выберите созданный PAT (для private репо) или оставьте пустым (для public)
   - Owner: ваш GitHub username или organization
   - Repository: название репозитория
   - Behaviours: оставить по умолчанию
4. Build Configuration:
   - Mode: `by Jenkinsfile`
   - Script Path: `Jenkinsfile`
5. Scan Multibranch Pipeline Triggers:
   - Выберите "Periodically if not otherwise run"
   - Interval: `1 hour` (fallback, если webhook не работает)

### Настройка Webhook (для автоматического запуска при push)

Так как Jenkins запущен локально, для работы webhook необходимо сделать его доступным извне. Есть два варианта:

#### Вариант 1: ngrok (рекомендуется для демо)

1. Установите ngrok: https://ngrok.com/download
2. Запустите туннель:

```bash
ngrok http 8080
```

3. Скопируйте HTTPS URL (например, `https://abc123.ngrok.io`)
4. В GitHub: Settings → Webhooks → Add webhook
   - Payload URL: `https://abc123.ngrok.io/github-webhook/`
   - Content type: `application/json`
   - Events: `Just the push event`
   - Active: ✓
5. В Jenkins: настройте GitHub Branch Source с включённым webhook (обычно включён по умолчанию)

**Важно**: URL ngrok меняется при каждом перезапуске. Для постоянного использования рассмотрите платный план с фиксированным доменом.

#### Вариант 2: Cloudflare Tunnel

Альтернатива ngrok с возможностью бесплатного фиксированного домена:

1. Установите `cloudflared`: https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/installation/
2. Создайте туннель:

```bash
cloudflared tunnel --url http://localhost:8080
```

3. Используйте полученный URL в настройках GitHub webhook аналогично ngrok

#### Вариант 3: Poll SCM (fallback, если webhook невозможен)

Если webhook настроить невозможно, используйте Poll SCM:

1. В настройках Multibranch Pipeline → Scan Multibranch Pipeline Triggers
2. Выберите "Periodically if not otherwise run"
3. Interval: `H/5 * * * *` (каждые 5 минут) или `*/5 * * * *` (каждые 5 минут)

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

### 3. Изменение версии и автоматический деплой

1. Измените версию в одном из сервисов (например, в `services/workspace-service/src/main/resources/application.properties`):
   ```properties
   app.version=1.0.1
   ```

2. Закоммитьте изменения:

```bash
git add .
git commit -m "Update version to 1.0.1"
git push origin main
```

3. Запустите pipeline в Jenkins:
   - Откройте http://localhost:8080
   - Выберите job `microservices-pipeline`
   - Нажмите "Build Now"
   - Или дождитесь автоматического запуска (если настроен polling/webhook)

4. Pipeline выполнит:
   - Checkout репозитория
   - Build (сборка Docker образов)
   - Deploy (перезапуск через docker compose)
   - Smoke tests (проверка health и version)

5. Проверьте обновление версии:

```bash
curl http://localhost:8081/api/version
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

