package de.jadenk.springcloud.util;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final Map<String, String> errorMessages = Map.of(
            "changepassword.current.invalid", "Current Password is wrong!",
            "changepassword.different.passwords", "There are 2 different Passwords!",
            "changepassword.same.password", "You cant Use the same Password as before!",
            "auth.invalid", "Username or password is incorrect.",
            "settings.upload.error", "Error while uploading Profile Picture, try again later.",
            "register.username.exists", "Username already exists."
    );

    private final Map<String, String> logMessages = new HashMap<>() {{
        put("dashboard.file.downloaded", "File downloaded: %s");
        put("dashboard.file.deleted", "File deleted: %s");
        put("dashboard.file.upload", "File uploaded: %s");
        put("sharing.file", "Shared File %s (id=%s) -> %s");
        put("register.user.missing.permissions", "Access attempt to /register");
        put("register.success", "User registered");
        put("login.success", "User logged in");
        put("admin.user.missing.permissions", "Access attempt to /admin");
        put("admin.user.email.changed", "Email geändert für USER: '%s' -> %s");
        put("admin.user.username.changed", "Username geändert für USER: '%s' -> %s");
        put("admin.user.role.changed", "Role Change for USER: '%s' Role: -> %s  -> %s");
        put("admin.user.password.changed", "Password Change for USER: '%s'");
        put("admin.user.ban.status", "User: '%s' was %s");
        put("admin.user.deleted", "Deleted User: '%s'");
    }};


    public String getError(String key) {
        return errorMessages.getOrDefault(key, "Error loading error messages!");
    }

    public String getError(String key, Object... args) {
        String template = errorMessages.getOrDefault(key, "Error loading error messages!");
        return String.format(template, args);
    }

    public String getLog(String key) {
        return logMessages.getOrDefault(key, "Error loading log messages!");
    }

    public String getLog(String key, Object... args) {
        String template = logMessages.getOrDefault(key, "Error loading log messages!");
        return String.format(template, args);
    }

}
