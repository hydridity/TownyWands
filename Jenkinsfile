pipeline {
    agent any
    tools {
        maven 'Maven 3.5'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}