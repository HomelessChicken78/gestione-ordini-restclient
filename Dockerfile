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
ENTRYPOINT ["java", "-jar", "gestione-ordini.jar"]

# docker build -t <username di docker hub>/<nome applicazione>:<versione> .
# Serv e a creare un'immagine con l'username di docker hub, quel nome dell'applicazione e quella versione a partire da quel file
# docker push <username di docker hub>/<nome applicazione>:<versione>
# Pusha l'immagine su docker hub
# Dopo possiamo runnare il container
# docker run -d --name gestione-ordini-container -p 8081:8080 <username di docker hub>/<nome applicazione>:<versione>
# Problema: nel nostro application.properties usiamo delle variabili di ambiente.
# se facciamo docker logs gestione-ordini-container possiamo vedere che da errori relativi a ciò
# Per cui dobbiamo fare
# docker run -d --name gestione-ordini-container -p 8081:8080 -e DB_URL=jdbc:mysql://localhost:3306/esempio_db -e DB_USER=user -e DB_PASSWORD=my_password -e RABBITMQ_USERNAME=username_rabbitmq -e RABBITMQ_PASSWORD=password_rabbitmq <username di docker hub>/<nome applicazione>:<versione>
# Attenzione però: quando facciamo localhost:3306, stiamo dicendo al container di cercare l'url del database all'interno di se stesso
# ma non esiste. Invece di quello dobbiamo dire: cerca l'url nell'altro container (dove si trova mysql).
# Quindi facciamo
# docker run -d --name gestione-ordini-container -p 8081:8080 -e DB_URL=jdbc:mysql://mysql-container:3306/esempio_db -e DB_USER=user -e DB_PASSWORD=my_password -e RABBITMQ_USERNAME=username_rabbitmq -e RABBITMQ_PASSWORD=password_rabbitmq <username di docker hub>/<nome applicazione>:<versione>