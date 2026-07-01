package it.itsacademy.gestioneordinirestclient.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Prendi la richiesta HTTP corrente dal thread
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest req = attributes.getRequest();

            String username = req.getHeader("X-Authenticated-User");

            if (username != null && !username.trim().isEmpty())
                return Optional.of(username);
        }

        // Quando RabbitMQ riceve il pagamento ed esegue repositoryOrdine.save(ordine),
        // quel thread è in background e NON ha una richiesta HTTP.
        // Se non mettiamo questo fallback, il salvataggio asincrono andrà in crash.
        return Optional.of("SYSTEM_RABBITMQ");
    }
}