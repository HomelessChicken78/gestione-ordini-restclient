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

# Per far comunicare tra loro più container (ad esempio la nostra app Spring Boot e MySQL),
# Docker usa le network.
# Ogni container può essere collegato ad una rete Docker.

# Docker mette a disposizione diversi driver di rete:
# - bridge: È il driver di rete di default di Docker, che crea una sottorete privata e isolata all'interno del tuo PC.
# Nella rete bridge creata di default ("bridge") NON c'è risoluzione DNS automatica.
# Tuttavia, se crei una tua rete bridge personalizzata (es. docker network create mia_rete),
# i container al suo interno possono comunicare tra loro usando direttamente il loro nome
# (es. mysql-container) perché Docker usa un DNS integrato per risolverne automaticamente gli IP.
# - host (host.docker.internal): Rimuove del tutto l'isolamento di rete tra il container e la macchina che lo ospita.
# Il container userà le stesse porte del tuo PC reale. Nota bene su "host.docker.internal": è un indirizzo speciale
# (usato molto su Docker Desktop per Windows e Mac) che puoi scrivere DENTRO al container al posto di "localhost" per
# indicare al container di collegarsi a un servizio (es. un database) che sta girando direttamente sul tuo PC, fuori da Docker.
# - none: Disabilita completamente qualsiasi interfaccia di rete. Il container non avrà accesso a internet,
# né alla rete locale, né agli altri container. È totalmente isolato dal mondo esterno.
# Si usa raramente, di solito per container che devono fare elaborazioni sicure offline o task ultra-specifici senza bisogno di comunicare.

# Siccome andremo a usare una rete possiamo fare
# docker network create my-net

# Per creare la rete. Poi connettere i container di my sql e rabbitmq alla network
# docker network connect my-net mysql-container
# docker network connect my-net rabbit1mq-container
# Infine possiamo connettere anche il nostro container del microservizio:
# docker network connect my-net gestione-ordini-container