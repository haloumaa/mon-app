pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "debbabiahlem/mon-app"
        POSTGRES_USER = 'postgres'
        POSTGRES_PASSWORD = 'postgres'
        POSTGRES_DB = 'monapp_test'
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
                script {
                    sh '''
                        docker run --rm --name postgres-test -d \
                            -e POSTGRES_USER=${POSTGRES_USER} \
                            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
                            -e POSTGRES_DB=${POSTGRES_DB} \
                            -p 5432:5432 \
                            postgres:16
                        sleep 8
                    '''
                }
                sh '''
                    mvn test \
                        -Dspring.datasource.url=jdbc:postgresql://localhost:5432/monapp_test \
                        -Dspring.datasource.username=postgres \
                        -Dspring.datasource.password=postgres \
                        -Dspring.jpa.hibernate.ddl-auto=create-drop
                '''
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
                sh '''
                    docker stop mon-app || true
                    docker rm mon-app || true
                '''
                sh "docker run -d --name mon-app -p 8082:8080 ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }
    }

    post {
        success {
            echo "Build #${BUILD_NUMBER} reussi pour mon-app"
        }
        failure {
            echo "Build #${BUILD_NUMBER} echoue pour mon-app"
        }
        always {
            cleanWs()
        }
    }
}
