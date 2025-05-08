pipeline {
    agent any

    environment {
        // Docker Hub credentials ID stored in Jenkins
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials') // Ensure this ID matches Jenkins credential ID
        // Docker Hub repository details
        DOCKER_IMAGE = 'cynthia3312/teedy-app' // Replace with your Docker Hub username and repository
        DOCKER_TAG = "${env.BUILD_NUMBER}" // Use Jenkins build number as tag
    }

    stages {
        // Stage 1: Build the application
        stage('Build') {
            steps {
                // Checkout code from GitHub
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/zxx3312/Teedy.git']] // Replace with your GitHub repo
                )
                // Run Maven build, skip tests
                sh 'mvn -B -DskipTests clean package'
            }
        }

        // Stage 2: Build Docker image
        stage('Building image') {
            steps {
                script {
                    // Build Docker image using Dockerfile in the root directory
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // Stage 3: Push Docker image to Docker Hub
        stage('Upload image') {
            steps {
                script {
                    // Log in to Docker Hub and push image
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub_credentials') {
                        // Push image with build number tag
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        // Optionally push 'latest' tag
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        // Stage 4: Run Docker container
        stage('Run containers') {
            steps {
                script {
                    // Stop and remove existing container if it exists
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'
                    // Run new container
                    sh "docker run --name teedy-container-8081 -d -p 8081:8080 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    // List all containers with name matching 'teedy-container'
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }

    // Post-build actions
    post {
        always {
            // Clean up workspace after build
            cleanWs()
        }
        failure {
            // Notify on failure (optional: configure email or other notifications)
            echo 'Build or deployment failed!'
        }
    }
}
