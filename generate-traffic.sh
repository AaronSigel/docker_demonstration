#!/bin/bash

# Скрипт генерации трафика для демонстрации метрик

echo "Генерация трафика для микросервисов..."

# Функция для проверки доступности сервиса
wait_for_service() {
    local port=$1
    local service=$2
    local max_attempts=30
    local attempt=0

    echo "Ожидание запуска $service..."
    while [ $attempt -lt $max_attempts ]; do
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "$service готов!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    echo "Ошибка: $service не запустился"
    return 1
}

# Ожидание запуска всех сервисов
wait_for_service 8081 "workspace-service"
wait_for_service 8082 "booking-service"
wait_for_service 8083 "payment-service"

echo "Начало генерации трафика..."

# Генерация трафика в течение 60 секунд
for i in {1..60}; do
    # Workspace Service
    curl -s -X POST http://localhost:8081/api/workspaces \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"Workspace $i\",\"description\":\"Test workspace $i\"}" > /dev/null
    
    curl -s http://localhost:8081/api/workspaces > /dev/null
    
    # Booking Service
    curl -s -X POST http://localhost:8082/api/bookings \
        -H "Content-Type: application/json" \
        -d "{\"workspaceId\":$((i % 10 + 1)),\"userId\":\"user$i\"}" > /dev/null
    
    curl -s http://localhost:8082/api/bookings/$((i % 5 + 1)) > /dev/null
    
    # Payment Service
    amount=$(awk "BEGIN {printf \"%.1f\", $i * 10.5}")
    curl -s -X POST http://localhost:8083/api/payments \
        -H "Content-Type: application/json" \
        -d "{\"bookingId\":$((i % 5 + 1)),\"amount\":$amount}" > /dev/null
    
    curl -s http://localhost:8083/api/payments/$((i % 5 + 1)) > /dev/null
    
    # Версии
    curl -s http://localhost:8081/api/version > /dev/null
    curl -s http://localhost:8082/api/version > /dev/null
    curl -s http://localhost:8083/api/version > /dev/null
    
    if [ $((i % 10)) -eq 0 ]; then
        echo "Обработано $i итераций..."
    fi
    
    sleep 1
done

echo "Генерация трафика завершена!"
echo "Проверьте метрики в Grafana: http://localhost:3000"

