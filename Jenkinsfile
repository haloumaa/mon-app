pipeline {
    agent any

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
                sh 'mvn test'
            }
        }

        stage('Build image Docker') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Push image') {
            steps {
                sh "echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin"
                sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
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
            slackSend(color: 'danger', message: "Build #${BUILD_NUMBER} échoué pour mon-app")
        }
        always {
            cleanWs()
        }
    }
}