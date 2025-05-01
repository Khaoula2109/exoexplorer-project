pipeline {
    agent {
        kubernetes {
            yaml '''
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: maven
                image: maven:3.8.4-openjdk-11
                command:
                - cat
                tty: true
              - name: node
                image: node:16
                command:
                - cat
                tty: true
              - name: docker
                image: docker:latest
                command:
                - cat
                tty: true
                volumeMounts:
                - name: docker-socket
                  mountPath: /var/run/docker.sock
              volumes:
              - name: docker-socket
                hostPath:
                  path: /var/run/docker.sock
            '''
        }
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Frontend') {
            steps {
                container('node') {
                    dir('frontend') {
                        sh 'npm install'
                        sh 'npm run build'
                    }
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                container('maven') {
                    dir('backend') {
                        sh 'mvn clean package -DskipTests'
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                container('docker') {
                    dir('frontend') {
                        sh 'docker build -t exoexplorer-frontend:${BUILD_NUMBER} .'
                    }
                    dir('backend') {
                        sh 'docker build -t exoexplorer-backend:${BUILD_NUMBER} .'
                    }
                }
            }
        }
        
        stage('Push Docker Images') {
            steps {
                container('docker') {
                    withCredentials([string(credentialsId: 'docker-registry-credentials', variable: 'DOCKER_AUTH')]) {
                        sh 'docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}'
                        sh 'docker tag exoexplorer-frontend:${BUILD_NUMBER} ${DOCKER_REGISTRY}/exoexplorer-frontend:${BUILD_NUMBER}'
                        sh 'docker tag exoexplorer-backend:${BUILD_NUMBER} ${DOCKER_REGISTRY}/exoexplorer-backend:${BUILD_NUMBER}'
                        sh 'docker push ${DOCKER_REGISTRY}/exoexplorer-frontend:${BUILD_NUMBER}'
                        sh 'docker push ${DOCKER_REGISTRY}/exoexplorer-backend:${BUILD_NUMBER}'
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                container('docker') {
                    sh '''
                    sed -i "s|image: .*exoexplorer-frontend:.*|image: ${DOCKER_REGISTRY}/exoexplorer-frontend:${BUILD_NUMBER}|g" kubernetes/frontend-deployment.yaml
                    sed -i "s|image: .*exoexplorer-backend:.*|image: ${DOCKER_REGISTRY}/exoexplorer-backend:${BUILD_NUMBER}|g" kubernetes/backend-deployment.yaml
                    kubectl apply -f kubernetes/
                    '''
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
