pipeline {
    agent any

    // Автоматический запуск сборки при обнаружении нового коммита через polling SCM
    triggers {
        pollSCM('H/1 * * * *')  // Проверка каждую минуту (H - хэш для распределения нагрузки)
    }

    environment {
        COMPOSE_PROJECT_NAME = 'demo'
        COMPOSE_FILE = 'docker-compose.yml'
        NETWORK_NAME = 'microservices_net'
        APP_SERVICES = 'workspace-service booking-service payment-service'
    }

    options {
        // Отключение параллельных сборок для предотвращения конфликтов при деплое
        disableConcurrentBuilds()
        // Добавление временных меток к выводу
        timestamps()
        // Таймаут на весь pipeline (30 минут)
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out repository...'
                checkout scm
            }
        }

        stage('Preflight') {
            steps {
                echo 'Checking Docker and Docker Compose availability...'
                sh """
                    set -euo pipefail
                    echo "Docker version:"
                    docker version --format '{{.Server.Version}}' || (echo "ERROR: Docker недоступен" && exit 1)
                    echo "Docker Compose version:"
                    docker compose version || (echo "ERROR: Docker Compose недоступен" && exit 1)
                    echo "Preflight checks passed"
                """
            }
        }

        stage('Build') {
            steps {
                echo 'Building Docker images for app services...'
                sh """
                    set -euo pipefail
                    docker compose -p ${COMPOSE_PROJECT_NAME} -f ${COMPOSE_FILE} build ${APP_SERVICES}
                """
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying app services...'
                sh """
                    set -euo pipefail
                    # Деплой только app-сервисов (jenkins, prometheus, grafana не затрагиваются)
                    docker compose -p ${COMPOSE_PROJECT_NAME} -f ${COMPOSE_FILE} up -d ${APP_SERVICES}
                """
            }
        }

        stage('Smoke Tests') {
            steps {
                echo 'Running smoke tests through Docker network...'
                script {
                    def services = [
                        ['name': 'workspace-service', 'port': '8081'],
                        ['name': 'booking-service', 'port': '8082'],
                        ['name': 'payment-service', 'port': '8083']
                    ]

                    services.each { service ->
                        echo "Testing ${service.name}..."

                        // Важно: НЕ используем localhost внутри контейнера Jenkins.
                        // Проверки выполняются через отдельный curl-контейнер в общей docker-сети.
                        // Добавлен retry, т.к. сервис может быть ещё в прогреве после up -d.
                        sh """
                            set -euo pipefail
                            echo "Checking health endpoint for ${service.name}..."
                            for i in \$(seq 1 30); do
                              docker run --rm --network ${NETWORK_NAME} curlimages/curl:8.5.0 \
                                --connect-timeout 2 --max-time 5 -fsS \
                                http://${service.name}:${service.port}/actuator/health >/dev/null && break
                              if [ "\$i" -eq 30 ]; then
                                echo "ERROR: Healthcheck timeout for ${service.name}"
                                exit 1
                              fi
                              sleep 2
                            done

                            echo "Checking version endpoint for ${service.name}..."
                            for i in \$(seq 1 30); do
                              docker run --rm --network ${NETWORK_NAME} curlimages/curl:8.5.0 \
                                --connect-timeout 2 --max-time 5 -fsS \
                                http://${service.name}:${service.port}/api/version >/dev/null && break
                              if [ "\$i" -eq 30 ]; then
                                echo "ERROR: Version check timeout for ${service.name}"
                                exit 1
                              fi
                              sleep 2
                            done
                        """

                        echo "${service.name} is UP and healthy"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo '=== Post-build actions ==='
            script {
                // Вывод статуса контейнеров
                echo 'Current container status:'
                sh """
                    docker compose -p ${COMPOSE_PROJECT_NAME} -f ${COMPOSE_FILE} ps || true
                """
                
                // Вывод последних логов сервисов (по 200 строк)
                echo '=== Recent logs from app services ==='
                def services = APP_SERVICES.split(' ')
                services.each { service ->
                    echo "--- Logs from ${service} (last 200 lines) ---"
                    sh """
                        docker compose -p ${COMPOSE_PROJECT_NAME} -f ${COMPOSE_FILE} logs --tail=200 ${service} || true
                    """
                }
            }
        }
    }
}

