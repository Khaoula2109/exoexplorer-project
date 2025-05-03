pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
        VERSION          = "${env.BUILD_NUMBER}"
        DOCKERHUB_USERNAME = "khaoula2109"

        # Node 18 installé via NVM
        PATH   = "$PATH:/var/jenkins_home/.nvm/versions/node/v18.18.0/bin"
        # Docker CLI
        DOCKER_HOST = "tcp://localhost:2375"
        DOCKER_PATH = "/usr/bin/docker"
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Install Node.js') {
            steps {
                sh '''
                curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
                export NVM_DIR="$HOME/.nvm"
                [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
                nvm install 18
                node -v && npm -v
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                sh '''
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
                echo "Vérification de Docker"
                ${DOCKER_PATH} --version || echo "Pas de Docker à ${DOCKER_PATH}"
                /usr/local/bin/docker --version || echo "Pas de Docker à /usr/local/bin"
                '''
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                ${DOCKER_PATH} build -t ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ./frontend
                ${DOCKER_PATH} build -t ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ./backend
                ${DOCKER_PATH} tag ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                ${DOCKER_PATH} tag ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION} ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                ${DOCKER_PATH} images | grep ${DOCKERHUB_USERNAME}
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh '''
                echo $DOCKER_HUB_CREDS_PSW | ${DOCKER_PATH} login -u $DOCKER_HUB_CREDS_USR --password-stdin
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-frontend:latest
                ${DOCKER_PATH} push ${DOCKERHUB_USERNAME}/exoexplorer-backend:latest
                '''
            }
        }

        /* ---------- PATCH OPTION 1 : kubectl dans le workspace ---------- */
        stage('Update Kubernetes Deployments') {
            steps {
                sh '''
                # Télécharger kubectl localement si absent
                if ! command -v kubectl >/dev/null 2>&1; then
                    curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                    chmod +x kubectl
                    export PATH="$PATH:$PWD"         # on l’ajoute au PATH pour cette session
                fi

                # Mettre à jour les tags dans les manifestes
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-backend:${VERSION}|" kubernetes/backend-deployment.yaml
                sed -i "s|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:.*|image: ${DOCKERHUB_USERNAME}/exoexplorer-frontend:${VERSION}|" kubernetes/frontend-deployment.yaml

                # Appliquer sur le cluster
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
        /* ---------------------------------------------------------------- */

        stage('Cleanup') {
            steps {
                sh '${DOCKER_PATH} system prune -af --volumes || true'
                cleanWs()
            }
        }
    }

    post {
        success { echo 'Déploiement réussi!' }
        failure { echo 'Le pipeline a échoué. Veuillez vérifier les logs.' }
        always  { echo 'Le pipeline est terminé.' }
    }
}

