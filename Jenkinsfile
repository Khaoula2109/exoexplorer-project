pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
        VERSION = "${env.BUILD_NUMBER}"
        DOCKERHUB_USERNAME = "khaoula2109"
        PATH = "$PATH:/var/jenkins_home/.nvm/versions/node/v18.18.0/bin"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Install Node.js') {
            steps {
                sh '''
                # Install NVM (Node Version Manager)
                curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
                
                # Add NVM to PATH for this session
                export NVM_DIR="$HOME/.nvm"
                [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
                
                # Install Node.js
                nvm install 18
                
                # Verify installation
                node -v
                npm -v
                '''
            }
        }
        
        stage('Build Frontend') {
            steps {
                sh '''
                # Add NVM to PATH for this session
                export NVM_DIR="$HOME/.nvm"
                [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
                
                cd frontend
                npm install
                npm run build
                '''
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
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
                sh 'kubectl apply -f kubernetes/db-secrets.yaml'
                sh 'kubectl apply -f kubernetes/oracle-wallet-configmap.yaml'
                sh 'kubectl apply -f kubernetes/backend-deployment.yaml'
                sh 'kubectl apply -f kubernetes/frontend-deployment.yaml'
                sh 'kubectl apply -f kubernetes/ingress.yaml'
                sh 'kubectl apply -f kubernetes/hpa.yaml'
            }
        }
        
        // Ajout d'une étape finale pour nettoyer l'espace de travail
        stage('Cleanup') {
            steps {
                cleanWs()
            }
        }
    }
    
    post {
        success {
            echo 'Déploiement réussi!'
        }
        failure {
            echo 'Le pipeline a échoué. Veuillez vérifier les logs.'
        }
        always {
            echo "Le pipeline est terminé."
        }
    }
}
