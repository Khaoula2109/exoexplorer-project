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
        
        stage('Install Docker') {
            steps {
                sh '''
                # Vérifier si Docker est déjà installé
                if ! command -v docker &> /dev/null; then
                    echo "Docker n'est pas installé. Installation en cours..."
                    # Installer les prérequis
                    sudo apt-get update
                    sudo apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
                    
                    # Ajouter la clé GPG Docker officielle
                    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
                    
                    # Configurer le dépôt stable
                    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
                    
                    # Installer Docker
                    sudo apt-get update
                    sudo apt-get install -y docker-ce docker-ce-cli containerd.io
                    
                    # Ajouter l'utilisateur jenkins au groupe docker
                    sudo usermod -aG docker jenkins
                    
                    # Redémarrer Docker
                    sudo systemctl restart docker
                    
                    # Attendre que le service Docker soit prêt
                    sleep 5
                fi
                
                # Vérifier l'installation
                docker --version
                '''
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                # Assurer que docker peut être utilisé sans sudo
                # Si l'utilisateur a été ajouté au groupe docker, mais n'a pas encore les permissions
                if ! docker ps &> /dev/null; then
                    echo "Utilisation de sudo pour docker..."
                    sudo docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
                    sudo docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend
                    
                    sudo docker tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                    sudo docker tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                else
                    docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
                    docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend
                    
                    docker tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                    docker tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                fi
                '''
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                sh '''
                if ! docker ps &> /dev/null; then
                    echo "Utilisation de sudo pour docker..."
                    echo $DOCKER_HUB_CREDS_PSW | sudo docker login -u $DOCKER_HUB_CREDS_USR --password-stdin
                    
                    sudo docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
                    sudo docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
                    sudo docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                    sudo docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                else
                    echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin
                    
                    docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
                    docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
                    docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                    docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                fi
                '''
            }
        }
        
        stage('Install kubectl') {
            steps {
                sh '''
                # Vérifier si kubectl est déjà installé
                if ! command -v kubectl &> /dev/null; then
                    echo "kubectl n'est pas installé. Installation en cours..."
                    
                    # Télécharger kubectl
                    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                    
                    # Rendre le binaire exécutable
                    chmod +x kubectl
                    
                    # Déplacer kubectl vers un répertoire dans le PATH
                    sudo mv kubectl /usr/local/bin/
                fi
                
                # Vérifier l'installation
                kubectl version --client
                '''
            }
        }
        
        stage('Update Kubernetes Deployments') {
            steps {
                sh '''
                # Assurer que le répertoire kubernetes existe
                if [ ! -d "kubernetes" ]; then
                    echo "Le répertoire kubernetes n'existe pas. Veuillez vérifier votre structure de projet."
                    exit 1
                fi
                
                # Mise à jour des manifestes Kubernetes
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|" kubernetes/backend-deployment.yaml
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|" kubernetes/frontend-deployment.yaml
                
                # Vérifier si le contexte Kubernetes est configuré
                kubectl config current-context || { echo "Contexte Kubernetes non configuré"; exit 1; }
                
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
                sh '''
                # Nettoyer les images Docker locales pour libérer de l'espace
                if command -v docker &> /dev/null; then
                    if ! docker ps &> /dev/null; then
                        sudo docker system prune -af --volumes
                    else
                        docker system prune -af --volumes
                    fi
                fi
                '''
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
