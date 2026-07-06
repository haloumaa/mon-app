pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "debbabiahlem/mon-app"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'docker stop postgres-test || true'
                sh 'docker rm postgres-test || true'
                sh 'docker run --rm --name postgres-test -d -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=monapp_test -p 5432:5432 postgres:16'
                sh 'for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30; do docker exec postgres-test pg_isready -U postgres > /dev/null 2>&1 && break; sleep 1; done'
                sh 'sleep 2'
                sh 'mvn test -Dspring.datasource.url=jdbc:postgresql://localhost:5432/monapp_test -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -Dspring.jpa.hibernate.ddl-auto=create-drop'
            }
            post {
                always {
                    sh 'docker stop postgres-test || true'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push Image') {
            steps {
                sh "echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin"
                sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker stop mon-app || true'
                sh 'docker rm mon-app || true'
                sh "docker run -d --name mon-app -p 8082:8080 ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }
    }

    post {
        success {
            echo "Build reussi pour mon-app"
        }
        failure {
            echo "Build echoue pour mon-app"
        }
        always {
            cleanWs()
        }
    }
}
