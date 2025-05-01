

# Étape de construction
FROM maven:3.8.4-openjdk-11 as build

WORKDIR /app

# Copier les fichiers POM et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le code source et compiler
COPY src ./src
RUN mvn package -DskipTests

# Étape de production
FROM openjdk:11-jre-slim

WORKDIR /app

# Copier le JAR construit
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8080

# Activer les métriques pour Prometheus
ENV JAVA_OPTS="-Dspring.profiles.active=prod -Dmanagement.endpoints.web.exposure.include=health,info,prometheus"

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
