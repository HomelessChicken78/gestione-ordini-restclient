package it.itsacademy.gestioneordinirestclient.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    /*
     * Definisce una coda (Queue) in RabbitMQ dove i messaggi verranno salvati
     * in attesa di essere consumati.
     * Il parametro "true" indica che la coda è durevole,
     * ovvero sopravviverà a un eventuale riavvio del server RabbitMQ.
     * Senza volume se si distrugge il container si perde per sempre comunque la coda.
     */
    @Bean public Queue queue()
    {
        return new Queue("payments.order.queue", true);
    }

    /*
     * Definisce un Exchange di tipo "Direct".
     * In RabbitMQ, i produttori non inviano mai messaggi direttamente alle code,
     * ma li inviano agli Exchange. Un DirectExchange instrada i messaggi
     * verso una specifica coda basandosi su una "routing key" esatta.
     */
    @Bean public DirectExchange exchange()
    {
        return new DirectExchange("payments.exchange");
    }

    /*
     * Questo metodo definisce il "Binding", ovvero la regola di collegamento
     * tra l'Exchange e la Coda definiti nei metodi precedenti.
     * Spring inietta automaticamente la 'queue' e l''exchange' definiti sopra
     * come parametri di questo metodo.
     * Un oggetto Binding che dice a RabbitMQ: "Prendi i messaggi in arrivo
     * sull'exchange "payments.exchange" che hanno come routing key esatta
     * "payments.order.created" e mettili nella coda 'payments.order.queue'".
     */
    @Bean
    public Binding binding(Queue queue, Exchange exchange)
    {
        // Utilizza il BindingBuilder fornito da Spring AMQP per costruire la regola in modo fluido
        return BindingBuilder.bind(queue)                 // Collega questa specifica coda...
                .to(exchange)                             // ...a questo specifico exchange...
                .with("payments.order.created")           // ...usando questa precisa routing key (chiave di instradamento)...
                .noargs();
    }
}
