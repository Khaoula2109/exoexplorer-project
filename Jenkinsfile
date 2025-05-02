pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
        VERSION = "${env.BUILD_NUMBER}"
        DOCKERHUB_USERNAME = "your-dockerhub-username"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh './mvnw clean package -DskipTests'
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh 'docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend'
                sh 'docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend'
                
                sh 'docker tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest'
                sh 'docker tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest'
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
                
                sh 'docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}'
                sh 'docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}'
                sh 'docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest'
                sh 'docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest'
            }
        }
        
        stage('Update Kubernetes Deployments') {
            steps {
                // Mise à jour des manifestes Kubernetes
                sh "sed -i 's|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|' kubernetes/backend-deployment.yaml"
                sh "sed -i 's|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|' kubernetes/frontend-deployment.yaml"
                
                // Déploiement sur Kubernetes
                sh 'kubectl apply -f kubernetes/namespace.yaml'
                sh 'kubectl apply -f kubernetes/secrets.yaml'
                sh 'kubectl apply -f kubernetes/wallet-configmap.yaml'
                sh 'kubectl apply -f kubernetes/backend-deployment.yaml'
                sh 'kubectl apply -f kubernetes/frontend-deployment.yaml'
                sh 'kubectl apply -f kubernetes/ingress.yaml'
                sh 'kubectl apply -f kubernetes/hpa.yaml'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Déploiement réussi!'
        }
        failure {
            echo 'Le pipeline a échoué. Veuillez vérifier les logs.'
        }
    }
}
