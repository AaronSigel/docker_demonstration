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

После первого запуска Jenkins необходимо выполнить первоначальную настройку:

1. Откройте http://localhost:8080
2. Получите начальный пароль администратора:

```bash
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

3. Установите рекомендуемые плагины
4. Создайте администратора
5. Установите необходимые плагины:
   - **Pipeline** (обычно уже установлен)
   - **Docker Pipeline** (для работы с Docker)
   - **Git** (для работы с Git репозиториями)

6. Настройте доступ Jenkins к Docker:
   - Jenkins уже имеет доступ к Docker socket через volume mount
   - Убедитесь, что Jenkins запущен с правами root (уже настроено в docker-compose.yml)

7. Создайте новый Pipeline Job:
   - New Item → Pipeline
   - Название: `microservices-pipeline`
   - В разделе Pipeline:
     - Definition: Pipeline script from SCM
     - SCM: Git
     - Repository URL: `https://github.com/AaronSigel/docker_demonstration.git` (или ваш URL)
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`

8. Настройте автоматический запуск (опционально):
   - В настройках Job → Build Triggers
   - Выберите "Poll SCM" и укажите `H/5 * * * *` (каждые 5 минут)
   - Или настройте webhook в GitHub/GitLab для автоматического запуска при push

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
docker network inspect final_task_default
```

