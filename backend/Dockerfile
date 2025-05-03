FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copier uniquement le JAR compilé
COPY target/exoExplorer-0.0.1-SNAPSHOT.jar app.jar

# Copier le wallet Oracle
#COPY src/main/resources/Wallet_exoExplorerDB /app/Wallet_exoExplorerDB

# Variable d'environnement pour le chemin du wallet
ENV TNS_ADMIN=/app/Wallet_exoExplorerDB

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
