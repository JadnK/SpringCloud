package de.jadenk.springcloud.util;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageService {

    private final Map<String, String> messages = Map.of(
            "auth.invalid", "Username or password is incorrect.",
            "register.username_exists", "Username already exists."
    );

    private final Map<String, String> logMessages = Map.of(
            "auth.invalid", "Benutzername oder Passwort ist falsch.",
            "auth.unauthorized", "Du bist nicht eingeloggt."
    );

    public String get(String key) {
        return messages.getOrDefault(key, "Error loading messages!");
    }

    public String getLog(String key) {
        return logMessages.getOrDefault(key, "Error loading log messages!");
    }
}
