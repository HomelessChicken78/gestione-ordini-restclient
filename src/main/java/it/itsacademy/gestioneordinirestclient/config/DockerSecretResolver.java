package it.itsacademy.gestioneordinirestclient.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DockerSecretResolver {
    private final Environment env;

    public String resolve(String key) {
        String property = env.getProperty(key);

        if (property == null) throw new IllegalStateException("Failed to find value for property: " + key);
        return property;
    }

    public String resolveFile(String key) {
        String filePath = resolve(key + "_FILE");

        try {
            if (filePath != null)
                return Files.readString(Path.of(filePath));
            else
                throw new IllegalStateException("Failed to find file path in property: " + key + "_FILE");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read secret file: " + filePath, e);
        }
    }
}
