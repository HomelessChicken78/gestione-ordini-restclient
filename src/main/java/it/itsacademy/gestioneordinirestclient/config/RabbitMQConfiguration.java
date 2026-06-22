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
    @Bean
    public Queue queueOrders() {
        return new Queue("payments.order.queue", true);
    }

    /*
    Altra coda usata dal consumer per comunicare che il pagamento è andato a buon fine
     */
    @Bean
    public Queue queuePaymentSuccess() {
        return new Queue("payments.success.queue", true);
    }

    /*
    Ultima coda usata dal consumer per comunicare che il pagamento è andato storto
     */
    @Bean
    public Queue queuePaymentFailure() {
        return new Queue("payments.failure.queue", true);
    }

    @Bean Queue queuePaymentSuccessEmail() {
        return new Queue("payment.success.email", true);
    }

    @Bean Queue queuePaymentFailureEmail() {
        return new Queue("payment.failure.email", true);
    }

    @Bean Queue queueReceipts() {
        return new Queue("receipts.file.queue");
    }

    @Bean Queue queueReport() {
        return new Queue("receipts.pdf.queue");
    }

    /*
     * Definisce un Exchange di tipo "Direct".
     * In RabbitMQ, i produttori non inviano mai messaggi direttamente alle code,
     * ma li inviano agli Exchange. Un DirectExchange instrada i messaggi
     * verso una specifica coda basandosi su una "routing key" esatta.
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("payments.exchange");
    }

    @Bean
    public DirectExchange exchangeRicevute() {
        return new DirectExchange("receipts.exchange");
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
    // Attenzione: spring deve capire quale Queue iniettare in questo binding. Per farlo spring legge il
    // nome del parametro e cerca un bean con lo stesso nome. Dunque queueOrders significa che andrà a cercare
    // un bean con nome queueOrders (che abbiamo definito sopra). In questo modo Spring usa la queue payments.order.queue
    @Bean
    public Binding ordersBinding(Queue queueOrders, Exchange exchange) {
        /*Quindi quello che succede: il producer invia al direct exchange un messaggio con payments.order.created.
        L'exchange visto che è direct riceve payments.order.created e cerca la coda che ha il binding
        che chiede la routing key payments.order.created.
        Lo trova e invia il messaggio sulla coda payments.order.queue
        */
        // Utilizza il BindingBuilder fornito da Spring AMQP per costruire la regola in modo fluido
        return BindingBuilder.bind(queueOrders)                 // Collega questa specifica coda...
                .to(exchange)                             // ...a questo specifico exchange...
                .with("payments.order.created")           // ...usando questa precisa routing key (chiave di instradamento)...
                .noargs();
    }

    /*
    Crea un altro binding per la queue payments.success.queue
     */
    // Attenzione: spring deve capire quale Queue iniettare in questo binding. Per farlo spring legge il
    // nome del parametro e cerca un bean con lo stesso nome. Dunque queuePaymentSuccess significa che andrà a cercare
    // un bean con nome queuePaymentSuccess (che abbiamo definito sopra). In questo modo Spring usa la queue payments.success.queue
    @Bean
    public Binding paymentSuccessBinding(Queue queuePaymentSuccess, Exchange exchange) {
        return BindingBuilder.bind(queuePaymentSuccess)
                .to(exchange)
                .with("payments.accettato")
                .noargs();
    }

    /*
    Crea un ultimo binding per la queue payments.failure.queue
     */
    // Attenzione: spring deve capire quale Queue iniettare in questo binding. Per farlo spring legge il
    // nome del parametro e cerca un bean con lo stesso nome. Dunque queuePaymentFailure significa che andrà a cercare
    // un bean con nome queuePaymentFailure (che abbiamo definito sopra). In questo modo Spring usa la queue payments.failure.queue
    @Bean
    public Binding paymentFailureBinding(Queue queuePaymentFailure, Exchange exchange) {
        return BindingBuilder.bind(queuePaymentFailure)
                .to(exchange)
                .with("payments.rifiutato")
                .noargs();
    }

    @Bean
    public Binding paymentSuccessEmailBinding(Queue queuePaymentSuccessEmail, Exchange exchange) {
        return BindingBuilder.bind(queuePaymentSuccessEmail)
                .to(exchange)
                .with("email.payment.accettato")
                .noargs();
    }

    @Bean Binding paymentFailureEmailBinding(Queue queuePaymentFailureEmail, Exchange exchange) {
        return BindingBuilder.bind(queuePaymentFailureEmail)
                .to(exchange)
                .with("email.payment.rifiutato")
                .noargs();
    }

    @Bean Binding sendReceiptBinding(Queue queueReceipts, Exchange exchangeRicevute) {
        return BindingBuilder.bind(queueReceipts)
                .to(exchangeRicevute)
                .with("receipts.file.create")
                .noargs();
    }

    @Bean Binding sendReportBinding(Queue queueReport, Exchange exchangeRicevute) {
        return BindingBuilder.bind(queueReport)
                .to(exchangeRicevute)
                .with("receipts.pdf.create")
                .noargs();
    }
}
