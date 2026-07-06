pipeline {
    agent any
    tools {
        maven 'Maven 3.9'
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "debbabiahlem/mon-app"
        PG_CONTAINER = "postgres-test-${env.BUILD_TAG}"
        NET_NAME = "ci-net"
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
                sh "docker network create ${NET_NAME} || true"
                sh "docker stop ${PG_CONTAINER} || true"
                sh "docker rm ${PG_CONTAINER} || true"

                sh """
                  docker run --rm --name ${PG_CONTAINER} -d \
                    --network ${NET_NAME} \
                    -e POSTGRES_USER=postgres \
                    -e POSTGRES_PASSWORD=postgres \
                    -e POSTGRES_DB=monapp_test \
                    postgres:16
                """

                sh "docker network connect ${NET_NAME} \$(hostname) || true"

                sh """
                  for i in \$(seq 1 30); do
                    docker exec ${PG_CONTAINER} pg_isready -U postgres > /dev/null 2>&1 && break
                    sleep 1
                  done
                """
                sh 'sleep 2'

                sh """
                  mvn test \
                    -Dspring.datasource.url=jdbc:postgresql://${PG_CONTAINER}:5432/monapp_test \
                    -Dspring.datasource.username=postgres \
                    -Dspring.datasource.password=postgres \
                    -Dspring.jpa.hibernate.ddl-auto=create-drop
                """
            }
            post {
                always {
                    sh "docker stop ${PG_CONTAINER} || true"
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
