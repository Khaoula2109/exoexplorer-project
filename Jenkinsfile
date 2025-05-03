pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
        VERSION = "${env.BUILD_NUMBER}"
        DOCKERHUB_USERNAME = "khaoula2109"
        PATH = "$PATH:/var/jenkins_home/.nvm/versions/node/v18.18.0/bin"
        DOCKER_HOST = "tcp://localhost:2375"  // Configuration du Docker host
        DOCKER_PATH = "/usr/bin/docker"       // Chemin complet vers l'exécutable Docker
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
        
        stage('Test Docker Connection') {
            steps {
                sh '''
                # Vérifiez que Docker est accessible
                echo "Vérification de Docker avec le chemin complet"
                ${DOCKER_PATH} --version || echo "Docker n'est pas accessible avec le chemin complet"
                
                # Tester avec d'autres chemins possibles
                /usr/local/bin/docker --version || echo "Docker n'est pas accessible dans /usr/local/bin"
                
                # Rechercher où Docker est installé
                find / -name docker -type f -executable 2>/dev/null || echo "Docker non trouvé dans le système de fichiers"
                '''
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                # Construction des images Docker
                ${DOCKER_PATH} build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
                ${DOCKER_PATH} build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend
                
                # Création des tags "latest"
                ${DOCKER_PATH} tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                ${DOCKER_PATH} tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                
                # Liste des images pour vérification
                ${DOCKER_PATH} images | grep ${DOCKERHUB_USERNAME}
                '''
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                sh '''
                # Connexion à Docker Hub
                echo $DOCKER_HUB_CREDS_PSW | ${DOCKER_PATH} login -u $DOCKER_HUB_CREDS_USR --password-stdin
                
                # Push des images vers Docker Hub
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                '''
            }
        }
        
        stage('Update Kubernetes Deployments') {
    steps {
        /* Charge le kubeconfig sécurisé dans $KCONF */
        withCredentials([file(credentialsId: 'exoexplorer-kubeconfig', variable: 'KCONF')]) {

            sh '''
            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            # 1) Prépare kubectl dans le PATH du job (pas besoin de sudo)
            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            export KUBECONFIG="$KCONF"

            if ! command -v kubectl >/dev/null 2>&1; then
                curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                chmod +x kubectl
                export PATH="$PATH:$PWD"
            fi

            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            # 2) Remplace les tags d’image par le numéro BUILD_NUMBER
            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|" kubernetes/backend-deployment.yaml
            sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|" kubernetes/frontend-deployment.yaml

            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            # 3) Applique les manifestes sur le cluster
            #––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
            kubectl apply -f kubernetes/namespace.yaml
            kubectl apply -f kubernetes/secrets.yaml
            kubectl apply -f kubernetes/wallet-configmap.yaml
            kubectl apply -f kubernetes/backend-deployment.yaml
            kubectl apply -f kubernetes/frontend-deployment.yaml
            kubectl apply -f kubernetes/ingress.yaml
            kubectl apply -f kubernetes/hpa.yaml
            '''
        }
    }
}

        stage('Cleanup') {
            steps {
                sh '''
                # Nettoyage des images locales pour économiser de l'espace disque
                ${DOCKER_PATH} system prune -af --volumes || true
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
