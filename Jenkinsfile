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

        

        // Autres stages...

    }

    

    post {

        always {

            script {

                node('any') {  

                    cleanWs()

                }

            }

            echo "Le pipeline est terminé."

        }

        success {

            echo 'Déploiement réussi!'

        }

        failure {

            echo 'Le pipeline a échoué. Veuillez vérifier les logs.'

        }

    }

}s
