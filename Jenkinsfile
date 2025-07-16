pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.11'
        jdk 'Java-21'  // Make sure Jenkins has a JDK 21 installation configured with this name
        // Alternative: Use 'jdk 'Java17'' if Java 21 is not available and update pom.xml java.version to 17
    }
    
    environment {
        DOCKER_IMAGE = 'aifij'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        IMAGE_VERSION="${env.BRANCH_NAME}"
        JAVA_HOME = "${tool 'Java-21'}"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        // DOCKER_REGISTRY = 'your-docker-registry' // Replace with your Docker registry
    }
    
    stages {
        stage('Verify Environment') {
            steps {
                script {
                    // Verify Java and Maven versions
                    sh 'java -version'
                    sh 'mvn -version'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    // Build the application
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    // Run tests
                    sh 'mvn test'
                }
            }
            post {
                always {
                    // Publish test results
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    // Package the application
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                 if (env.BRANCH_NAME.startsWith('release')) {
                        sh 'docker build -t ${DOCKER_IMAGE}:${IMAGE_VERSION} -t ${DOCKER_IMAGE}:latest .'
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