pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'final_task'
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
                echo 'Building Docker images...'
                sh '''
                    docker compose build --no-cache
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying services...'
                sh '''
                    docker compose up -d --build
                    sleep 30
                '''
            }
        }

        stage('Smoke Tests') {
            steps {
                echo 'Running smoke tests...'
                script {
                    def services = [
                        ['name': 'workspace-service', 'port': '8081'],
                        ['name': 'booking-service', 'port': '8082'],
                        ['name': 'payment-service', 'port': '8083']
                    ]

                    services.each { service ->
                        echo "Testing ${service.name}..."
                        
                        // Health check
                        sh """
                            curl -f -s http://localhost:${service.port}/actuator/health || exit 1
                        """
                        
                        // Version check
                        sh """
                            curl -f -s http://localhost:${service.port}/api/version || exit 1
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

