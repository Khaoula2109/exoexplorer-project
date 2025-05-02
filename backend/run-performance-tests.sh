#!/bin/bash
# Script optimisé pour exécuter les tests de performance avec le profil perf

# Couleurs pour une meilleure lisibilité
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================================${NC}"
echo -e "${YELLOW}      EXÉCUTION DES TESTS DE PERFORMANCE GATLING       ${NC}"
echo -e "${YELLOW}========================================================${NC}"

# 1. Vérifier si l'application est déjà en cours d'exécution avec le profil perf
PID=$(pgrep -f "spring.profiles.active=perf" || echo "")
if [ -n "$PID" ]; then
  echo -e "${YELLOW}Application déjà en cours d'exécution avec le profil perf (PID: $PID)${NC}"
  read -p "Voulez-vous redémarrer l'application avec le profil perf? (o/n): " restart
  if [[ $restart == "o" || $restart == "O" ]]; then
    echo -e "${YELLOW}Arrêt de l'application en cours...${NC}"
    kill $PID
    sleep 5
  else
    echo -e "${GREEN}Utilisation de l'instance en cours d'exécution.${NC}"
  fi
fi

# 2. Démarrer l'application avec le profil perf si nécessaire
if [ -z "$PID" ] || [[ $restart == "o" || $restart == "O" ]]; then
  echo -e "${YELLOW}Démarrage de l'application avec le profil perf et ressources optimisées...${NC}"
  
  # Démarrer l'application en arrière-plan avec plus de mémoire et paramètres GC optimisés
  nohup mvn spring-boot:run -Dspring-boot.run.profiles=perf -Dspring-boot.run.jvmArguments="-Xmx2G -Xms1G -XX:+UseG1GC -XX:MaxGCPauseMillis=200" > perf-app.log 2>&1 &
  APP_PID=$!
  
  echo -e "${GREEN}Application démarrée avec PID: $APP_PID${NC}"
  echo -e "${YELLOW}Attente du démarrage complet de l'application (45 secondes)...${NC}"
  sleep 45  # Augmenté pour donner plus de temps au démarrage avec les nouveaux paramètres JVM
fi

# 3. Vérifier que les dossiers existent
echo -e "${YELLOW}Vérification des dossiers pour les données de test...${NC}"
mkdir -p exoExplorer-project/backend/src/test/resources/data

# 4. Générer les fichiers de données pour Gatling
echo -e "${YELLOW}Génération des fichiers de données pour les tests Gatling...${NC}"

# Créer users.csv
echo "email,password" > src/test/resources/data/users.csv
for i in {1..50}; do
  echo "user${i}@example.com,password${i}" >> src/test/resources/data/users.csv
done
echo -e "${GREEN}✓ Fichier users.csv créé avec 50 utilisateurs${NC}"

# Créer exoplanets.csv
echo "exoId,name" > src/test/resources/data/exoplanets.csv
# Liste de noms d'exoplanètes
exoplanets=("Kepler-186f" "Proxima Centauri b" "TRAPPIST-1e" "TOI-700d" "HD 219134 b" 
          "K2-18b" "LHS 1140 b" "WASP-12b" "Kepler-22b" "Kepler-452b" 
          "WASP-121b" "HD 209458 b" "TrES-2b" "HR 8799 e" "HD 189733 b" 
          "GJ 1214 b" "WASP-39b" "51 Pegasi b" "HAT-P-1b" "CoRoT-7b")

for i in {1..20}; do
  echo "${i},${exoplanets[$i-1]}" >> src/test/resources/data/exoplanets.csv
done
echo -e "${GREEN}✓ Fichier exoplanets.csv créé avec 20 exoplanètes${NC}"

# Créer backup_codes.csv
echo "code" > src/test/resources/data/backup_codes.csv
for i in {1..30}; do
  # Génération de codes à 6 chiffres
  code=$(printf "%06d" $((RANDOM % 1000000)))
  echo "$code" >> src/test/resources/data/backup_codes.csv
done
echo -e "${GREEN}✓ Fichier backup_codes.csv créé avec 30 codes${NC}"

# 5. Créer un lien symbolique dans le dossier data à la racine (pour compatibilité)
echo -e "${YELLOW}Création d'un lien symbolique dans le dossier 'data' à la racine...${NC}"
mkdir -p data
ln -sf "$(pwd)/exoExplorer-project/backend/src/test/resources/data/users.csv" data/
ln -sf "$(pwd)/exoExplorer-project/backend/src/test/resources/data/exoplanets.csv" data/
ln -sf "$(pwd)/exoExplorer-project/backend/src/test/resources/data/backup_codes.csv" data/
echo -e "${GREEN}✓ Liens symboliques créés${NC}"

# 6. Exécuter les tests Gatling
echo -e "${YELLOW}========================================================${NC}"
echo -e "${YELLOW}      EXÉCUTION DES TESTS GATLING                       ${NC}"
echo -e "${YELLOW}========================================================${NC}"

# Exécuter le test de fumée d'abord avec 1 seul utilisateur
echo -e "${YELLOW}Exécution du test de fumée (1 seul utilisateur)...${NC}"
mvn gatling:test -Dgatling.simulationClass=simulations.SmokeSimulation -DbaseUrl=http://localhost:8080

# Si le test de fumée réussit, exécuter les autres tests
if [ $? -eq 0 ]; then
  echo -e "${GREEN}Le test de fumée a réussi. Exécution des autres tests...${NC}"
  
  # Demander à l'utilisateur quels tests exécuter
  echo -e "${YELLOW}Quels tests souhaitez-vous exécuter? ${NC}"
  echo "1. BaselineSimulation (Référence de performance - charge légère)"
  echo "2. LoadSimulation (Charge normale)"
  echo "3. StressSimulation (Test de stress - charge élevée)"
  echo "4. SpikeSimulation (Test de pics - charge soudaine)"
  echo "5. SoakSimulation (Test d'endurance - longue durée)"
  echo "6. Tous les tests (dans l'ordre croissant de charge)"
  echo "0. Annuler"
  
  read -p "Votre choix (0-6): " choice
  
  case $choice in
    1)
      echo -e "${YELLOW}Exécution de BaselineSimulation...${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.BaselineSimulation -DbaseUrl=http://localhost:8080
      ;;
    2)
      echo -e "${YELLOW}Exécution de LoadSimulation...${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation -DbaseUrl=http://localhost:8080
      ;;
    3)
      echo -e "${YELLOW}Exécution de StressSimulation...${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation -DbaseUrl=http://localhost:8080
      ;;
    4)
      echo -e "${YELLOW}Exécution de SpikeSimulation...${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation -DbaseUrl=http://localhost:8080
      ;;
    5)
      echo -e "${YELLOW}Exécution de SoakSimulation...${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.SoakSimulation -DbaseUrl=http://localhost:8080
      ;;
    6)
      echo -e "${YELLOW}Exécution de tous les tests de performance dans l'ordre croissant de charge...${NC}"
      echo -e "${YELLOW}Étape 1/5: BaselineSimulation${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.BaselineSimulation -DbaseUrl=http://localhost:8080
      
      echo -e "${YELLOW}Étape 2/5: LoadSimulation${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.LoadSimulation -DbaseUrl=http://localhost:8080
      
      echo -e "${YELLOW}Étape 3/5: SpikeSimulation${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.SpikeSimulation -DbaseUrl=http://localhost:8080
      
      echo -e "${YELLOW}Étape 4/5: StressSimulation${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.StressSimulation -DbaseUrl=http://localhost:8080
      
      echo -e "${YELLOW}Étape 5/5: SoakSimulation${NC}"
      mvn gatling:test -Dgatling.simulationClass=simulations.SoakSimulation -DbaseUrl=http://localhost:8080
      ;;
    0)
      echo -e "${YELLOW}Annulation des tests supplémentaires.${NC}"
      ;;
    *)
      echo -e "${RED}Choix invalide.${NC}"
      ;;
  esac
else
  echo -e "${RED}Le test de fumée a échoué. Vérifiez les problèmes avant de continuer.${NC}"
  echo -e "${YELLOW}Voulez-vous afficher la fin du log d'application pour diagnostiquer le problème? (o/n)${NC}"
  read -p "" show_logs
  if [[ $show_logs == "o" || $show_logs == "O" ]]; then
    echo -e "${YELLOW}Dernières 50 lignes du log d'application:${NC}"
    tail -n 50 perf-app.log
  fi
fi

# 7. Demander si l'on veut arrêter l'application
read -p "Voulez-vous arrêter l'application running avec le profil perf? (o/n): " stop
if [[ $stop == "o" || $stop == "O" ]]; then
  PID=$(pgrep -f "spring.profiles.active=perf" || echo "")
  if [ -n "$PID" ]; then
    echo -e "${YELLOW}Arrêt de l'application (PID: $PID)...${NC}"
    kill $PID
    sleep 2
    echo -e "${GREEN}Application arrêtée.${NC}"
  else
    echo -e "${RED}Application non trouvée en cours d'exécution.${NC}"
  fi
fi

echo -e "${GREEN}Tests de performance terminés!${NC}"
echo -e "${YELLOW}Les rapports sont disponibles dans le dossier target/gatling${NC}"

# 8. Ouvrir le dernier rapport généré
if [ -d "target/gatling" ]; then
  LAST_REPORT=$(find target/gatling -name "index.html" | sort -r | head -1)
  if [ -n "$LAST_REPORT" ]; then
    echo -e "${YELLOW}Ouverture du dernier rapport: $LAST_REPORT${NC}"
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
      xdg-open "$LAST_REPORT"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
      open "$LAST_REPORT"
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
      start "$LAST_REPORT"
    else
      echo -e "${YELLOW}Impossible d'ouvrir automatiquement le rapport.${NC}"
      echo -e "${YELLOW}Veuillez l'ouvrir manuellement: $LAST_REPORT${NC}"
    fi
  fi
fi

echo -e "${YELLOW}========================================================${NC}"
echo -e "${YELLOW}      NETSTAT - CONNEXIONS ACTIVES                      ${NC}"
echo -e "${YELLOW}========================================================${NC}"
echo -e "${YELLOW}Nombre de connexions actives sur le port 8080:${NC}"
netstat -an | grep 8080 | grep ESTABLISHED | wc -l

echo -e "${YELLOW}========================================================${NC}"
echo -e "${YELLOW}      STATISTIQUES MÉMOIRE JVM                          ${NC}"
echo -e "${YELLOW}========================================================${NC}"
jps -l | grep exoExplorer || echo "Application JVM non trouvée"