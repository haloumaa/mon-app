pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "debbabiahlem/mon-app"
        POSTGRES_USER = 'postgres'
        POSTGRES_PASSWORD = 'changeme123'
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
                        docker network create ci-network || true
                        docker stop postgres-test || true
                        docker rm postgres-test || true

                        docker run --rm --name postgres-test -d \
                            --network ci-network \
                            -e POSTGRES_USER=${POSTGRES_USER} \
                            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
                            -e POSTGRES_DB=${POSTGRES_DB} \
                            postgres:16

                        sleep 10
                    '''
                }
                sh '''
                    mvn test \
                        -Dspring.datasource.url=jdbc:postgresql://postgres-test:5432/monapp_test \
                        -Dspring.datasource.username=${POSTGRES_USER} \
                        -Dspring.datasource.password=${POSTGRES_PASSWORD} \
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
                    docker run -d --name mon-app -p 8082:8080 ''' + "${IMAGE_NAME}:${BUILD_NUMBER}" + '''
                '''
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
            sh 'docker network rm ci-network || true'
            cleanWs()
        }
    }
}
