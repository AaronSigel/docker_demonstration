pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'demo'
        NETWORK_NAME = 'microservices_net'
        APP_SERVICES = 'workspace-service booking-service payment-service'
    }

    options {
        // Чтобы деплой не конфликтовал сам с собой при нескольких событиях подряд (push/webhook/polling)
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out repository...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Docker images for app services...'
                sh """
                    set -euo pipefail
                    docker compose -p ${COMPOSE_PROJECT_NAME} build ${APP_SERVICES}
                """
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying app services...'
                sh """
                    set -euo pipefail
                    docker compose -p ${COMPOSE_PROJECT_NAME} up -d ${APP_SERVICES}
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
                            for i in \$(seq 1 30); do
                              docker run --rm --network ${NETWORK_NAME} curlimages/curl:8.5.0 \
                                --connect-timeout 2 --max-time 5 -fsS \
                                http://${service.name}:${service.port}/actuator/health >/dev/null && break
                              if [ "\$i" -eq 30 ]; then
                                echo "Healthcheck timeout for ${service.name}"
                                exit 1
                              fi
                              sleep 2
                            done

                            for i in \$(seq 1 30); do
                              docker run --rm --network ${NETWORK_NAME} curlimages/curl:8.5.0 \
                                --connect-timeout 2 --max-time 5 -fsS \
                                http://${service.name}:${service.port}/api/version >/dev/null && break
                              if [ "\$i" -eq 30 ]; then
                                echo "Version check timeout for ${service.name}"
                                exit 1
                              fi
                              sleep 2
                            done
                        """

                        echo "${service.name} is UP"
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
            echo 'Cleaning up...'
        }
    }
}

