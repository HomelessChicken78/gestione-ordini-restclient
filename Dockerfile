# Prendi dall'immagine eclipse-temurin:17-jre-jammy per avere la jdk
FROM eclipse-temurin:17-jre-jammy
# Spostati in /app
WORKDIR /app
# Prendi dal mio pc il .jar e mettilo nel container
COPY target/*.jar gestione-ordini.jar
# Esponi la porta 8080 all'esterno
EXPOSE 8080
# Runna questo comando al run del container
# Serve a lanciare java col jar
ENTRYPOINT ["java", "-jar", "/gestione-ordini.jar"]

# docker build -t <username di docker hub>/<nome applicazione>:<versione> .
# Serv e a creare un'immagine con l'username di docker hub, quel nome dell'applicazione e quella versione a partire da quel file