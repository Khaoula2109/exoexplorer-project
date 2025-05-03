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
                sh '''
                # Création d'un script Docker pour être exécuté sur l'hôte
                cat > docker-build.sh << 'EOF'
#!/bin/bash
set -e

# Variables
VERSION=$1
DOCKERHUB_USERNAME=$2

# Construction des images Docker
docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
docker build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend

# Création des tags "latest"
docker tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
docker tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest

# Liste des images pour vérification
docker images | grep ${DOCKERHUB_USERNAME}
EOF

                chmod +x docker-build.sh
                
                # Exécution du script (sur l'hôte ou via un mécanisme externe)
                echo "Pour exécuter manuellement: ./docker-build.sh ${VERSION} ${DOCKERHUB_USERNAME}"
                '''
            }
        }
        
        stage('Create Docker Push Script') {
            steps {
                sh '''
                # Création d'un script pour pousser les images Docker
                cat > docker-push.sh << 'EOF'
#!/bin/bash
set -e

# Variables
VERSION=$1
DOCKERHUB_USERNAME=$2
DOCKER_HUB_USR=$3
DOCKER_HUB_PSW=$4

# Connexion à Docker Hub
echo $DOCKER_HUB_PSW | docker login -u $DOCKER_HUB_USR --password-stdin

# Push des images vers Docker Hub
docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
docker push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
docker push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
EOF

                chmod +x docker-push.sh
                
                # Exécution du script (sur l'hôte ou via un mécanisme externe)
                echo "Pour exécuter manuellement: ./docker-push.sh ${VERSION} ${DOCKERHUB_USERNAME} votre-nom-utilisateur votre-mot-de-passe"
                '''
            }
        }
        
        stage('Update Kubernetes Deployments') {
            steps {
                sh '''
                # Mise à jour des manifestes Kubernetes
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|" kubernetes/backend-deployment.yaml
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|" kubernetes/frontend-deployment.yaml
                
                # Création d'un script pour le déploiement Kubernetes
                cat > k8s-deploy.sh << 'EOF'
#!/bin/bash
set -e

# Installation de kubectl si nécessaire
if ! command -v kubectl &> /dev/null; then
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    chmod +x kubectl
    mv kubectl /usr/local/bin/
fi

# Déploiement sur Kubernetes
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/db-secrets.yaml
kubectl apply -f kubernetes/oracle-wallet-configmap.yaml
kubectl apply -f kubernetes/backend-deployment.yaml
kubectl apply -f kubernetes/frontend-deployment.yaml
kubectl apply -f kubernetes/ingress.yaml
kubectl apply -f kubernetes/hpa.yaml

# Forcer le redémarrage des déploiements
kubectl rollout restart deployment/exoexplorer-backend -n exoexplorer
kubectl rollout restart deployment/exoexplorer-frontend -n exoexplorer
EOF

                chmod +x k8s-deploy.sh
                echo "Pour exécuter manuellement: ./k8s-deploy.sh"
                '''
            }
        }
        
        stage('Instructions pour le déploiement manuel') {
            steps {
                echo '''
                ==================== INSTRUCTIONS POUR LE DÉPLOIEMENT MANUEL ====================
                
                Comme Docker n'est pas disponible dans le conteneur Jenkins, vous devez exécuter les
                scripts générés manuellement sur l'hôte où Docker est installé:
                
                1. Exécutez le script de build Docker:
                   ./docker-build.sh ${VERSION} ${DOCKERHUB_USERNAME}
                
                2. Exécutez le script de push Docker:
                   ./docker-push.sh ${VERSION} ${DOCKERHUB_USERNAME} votre-nom-utilisateur votre-mot-de-passe
                
                3. Exécutez le script de déploiement Kubernetes:
                   ./k8s-deploy.sh
                
                Ces scripts ont été générés dans votre espace de travail Jenkins.
                ================================================================================
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Phase de préparation réussie! Veuillez suivre les instructions pour compléter le déploiement.'
        }
        failure {
            echo 'Le pipeline a échoué. Veuillez vérifier les logs.'
        }
        always {
            echo "Le pipeline est terminé."
        }
    }
}
