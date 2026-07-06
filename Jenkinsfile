pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                withMaven(maven: 'Maven 3.9') {   
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Test') {
            steps {
                withMaven(maven: 'Maven 3.9') {
                    sh 'mvn test'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}