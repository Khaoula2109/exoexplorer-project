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
        
        stage('Use Docker Container') {
            agent {
                docker {
                    image 'docker:latest'
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                    reuseNode true
                }
            }
            stages {
                stage('Build and Push Docker Images') {
                    steps {
                        withCredentials([string(credentialsId: 'docker-hub-credentials', variable: 'DOCKER_PASSWORD')]) {
                            sh '''
                            # Login to Docker Hub
                            echo $DOCKER_PASSWORD | docker login -u ${DOCKERHUB_USERNAME} --password-stdin
                            
                            # Build Docker images
                            docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
                            docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend
                            
                            # Tag images as latest
                            docker tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                            docker tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                            
                            # Push images to Docker Hub
                            docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
                            docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
                            docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                            docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Update Kubernetes Deployments') {
            agent {
                docker {
                    image 'bitnami/kubectl:latest'
                    reuseNode true
                }
            }
            steps {
                sh '''
                # Mise à jour des manifestes Kubernetes
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|" kubernetes/backend-deployment.yaml
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|" kubernetes/frontend-deployment.yaml
                
                # Déploiement sur Kubernetes
                kubectl apply -f kubernetes/namespace.yaml
                kubectl apply -f kubernetes/db-secrets.yaml
                kubectl apply -f kubernetes/oracle-wallet-configmap.yaml
                kubectl apply -f kubernetes/backend-deployment.yaml
                kubectl apply -f kubernetes/frontend-deployment.yaml
                kubectl apply -f kubernetes/ingress.yaml
                kubectl apply -f kubernetes/hpa.yaml
                '''
            }
        }
        
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
