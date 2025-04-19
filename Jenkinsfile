pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'aifij'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        // DOCKER_REGISTRY = 'your-docker-registry' // Replace with your Docker registry
    }
    
    stages {
        stage('Build') {
            steps {
                script {
                    // Build the application
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                 if (env.BRANCH_NAME.startsWith('release')) {
                        sh 'docker build -t ${DOCKER_IMAGE}:${env.BRANCH_NAME} .'
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
} 