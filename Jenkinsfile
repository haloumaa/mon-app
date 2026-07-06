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
                        docker stop postgres-test || true
                        docker rm postgres-test || true
                        
                        docker run --rm --name postgres-test -d \
                            -e POSTGRES_USER=${POSTGRES_USER} \
                            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
                            -e POSTGRES_DB=${POSTGRES_DB} \
                            -p 5432:5432 \
                            postgres:16
                        
                        sleep 10
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
                    sh 'docker rm postgres-test || true'
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
                script {
                    sh "echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin"
                    sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
                    sh "docker push ${IMAGE_NAME}:latest"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sshagent(['prod-server-ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no user@serveur_prod '
                        docker pull ${IMAGE_NAME}:${BUILD_NUMBER} &&
                        docker stop mon-app || true &&
                        docker rm mon-app || true &&
                        docker run -d --name mon-app -p 8080:8080 ${IMAGE_NAME}:${BUILD_NUMBER}
                        '
                    """
                }
            }
        }
    }
    
    post {
        success {
            slackSend(color: 'good', message: "Build #${BUILD_NUMBER} réussi pour mon-app")
        }
        failure {
            slackSend(color: 'danger', message: " Build #${BUILD_NUMBER} échoué pour mon-app")
        }
        always {
            cleanWs()
        }
    }
}