package it.itsacademy.gestioneordinirestclient.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.time.Duration;

@Configuration
public class GeneralConfigurations {
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
        return S3AsyncClient.builder()
                // Non serve mettere il Credential Provider, perchè prende quello definito sul vostro PC tramite AWS CLI
                //.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                // .credentialsProvider(DefaultCredentialsProvider.create()) NB: Non serve: lo fa da solo
                //Tempo di attesa massima per l'interazione con AWS
                .overrideConfiguration(b -> b.apiCallTimeout(Duration.ofMinutes(2)))
                .build();
    }
}
