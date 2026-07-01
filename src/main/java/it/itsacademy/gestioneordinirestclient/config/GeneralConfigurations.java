package it.itsacademy.gestioneordinirestclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Configuration @RequiredArgsConstructor
@EnableJpaAuditing(auditorAwareRef = "getAuditorAwareImpl")
public class GeneralConfigurations {
    private final DockerSecretResolver resolver;

    /*
    Contesto: Di default spring per convertire una stringa in json e viceversa usa un SimpleMessageConverter
    (eccezione fatta per una rest controller), che vuole una stringa e converte in un array di byte e viceversa,
    ma non è compatibile con gli oggetti.
    Jackson invece è un MessageConverter che converte gli oggetti in json direttamente.
    Con questo bean diciamo a spring: "Non usare il tuo SimpleMessageConverter che è vecchio. Usa Jackson".
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {
        // Cerca le env "AWS_ACCESS_KEY_ID_FILE" e "AWS_SECRET_ACCESS_KEY_FILE". A queste env corrisponde un path interno
        // al docker. Legge i file all'interno di quel path per cercare il valore. Ritorna quel valore contenente le credenziali.a
        String accessKey = resolver.resolveFile("AWS_ACCESS_KEY_ID");
        String secretKey = resolver.resolveFile("AWS_SECRET_ACCESS_KEY");

        return S3AsyncClient.builder()
                // Normalmente non serve mettere il Credential Provider, perchè prende quello definito sul vostro PC tramite AWS CLI.
                // in questo caso siamo dentro un container e dobbiamo usare docker secret quindi dobbiamo usarlo
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                // NB: Se usassimo le credenziali di default, non servirebbe: lo farebbe da solo
                // .credentialsProvider(DefaultCredentialsProvider.create())
                //Tempo di attesa massima per l'interazione con AWS
                .overrideConfiguration(b -> b.apiCallTimeout(Duration.ofMinutes(2)))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        // Cerca le env "AWS_ACCESS_KEY_ID_FILE" e "AWS_SECRET_ACCESS_KEY_FILE". A queste env corrisponde un path interno
        // al docker. Legge i file all'interno di quel path per cercare il valore. Ritorna quel valore contenente le credenziali.a
        String accessKey = resolver.resolveFile("AWS_ACCESS_KEY_ID");
        String secretKey = resolver.resolveFile("AWS_SECRET_ACCESS_KEY");

        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    // Dice dove prendere chi ha fatto le modifiche/la creazione
    @Bean
    AuditorAware<String> getAuditorAwareImpl(){
        return new AuditorAwareImpl();
    }
}
