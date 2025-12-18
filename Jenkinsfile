pipeline {
    agent any

    environment {
        // Set environment variables if needed
        CI = 'true'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Running on ${isUnix() ? 'Unix/Mac' : 'Windows'} agent"
                }
            }
        }

        stage('Backend: Build & Test') {
            steps {
                dir('Microservices-Backend') {
                    script {
                        if (isUnix()) {
                            // Mac/Linux
                            sh 'mvn clean install' // Runs tests by default
                        } else {
                            // Windows
                            bat 'mvn clean install'
                        }
                    }
                }
            }
            post {
                always {
                    // Archive JUnit test results
                    junit 'Microservices-Backend/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend: Install & Build') {
            steps {
                dir('Frontend') {
                    script {
                        if (isUnix()) {
                            // Mac/Linux
                            sh 'npm ci' // Clean install for CI
                            sh 'npm run build'
                        } else {
                            // Windows
                            bat 'npm ci'
                            bat 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Docker: Setup Buildx') {
            steps {
                script {
                    try {
                        if (isUnix()) {
                            sh 'docker buildx create --use --name multiplatform-builder || docker buildx use multiplatform-builder'
                        } else {
                            bat 'docker buildx create --use --name multiplatform-builder || docker buildx use multiplatform-builder'
                        }
                    } catch (Exception e) {
                        echo "Buildx setup failed. Continuing with default builder..."
                    }
                }
            }
        }

        stage('Docker: Build Multi-Platform Images') {
            steps {
                dir('Microservices-Backend') {
                    script {
                        try {
                            if (isUnix()) {
                                sh 'docker buildx bake --set *.platform=linux/amd64,linux/arm64 -f docker-compose.yml'
                            } else {
                                bat 'docker buildx bake --set *.platform=linux/amd64,linux/arm64 -f docker-compose.yml'
                            }
                        } catch (Exception e) {
                            echo "Multi-platform build failed. Falling back to single platform..."
                            if (isUnix()) {
                                sh 'docker-compose build'
                            } else {
                                bat 'docker-compose build'
                            }
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
