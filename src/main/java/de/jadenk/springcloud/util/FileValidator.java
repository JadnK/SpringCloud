package de.jadenk.springcloud.util;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    // verbotene Endungen
    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "cmd", "msi", "com", "php", "html"
    ));

    /**
     * Prüft, ob Datei gültig ist:
     * - Endung nicht in Blocklist
     * - Content-Type wird von Java erkannt
     */
    public static boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) return false;

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return false;
        String ext = filename.substring(dotIndex + 1).toLowerCase();

        if (BLOCKED_EXTENSIONS.contains(ext)) return false;

        String guessedType = URLConnection.guessContentTypeFromName(filename);
        if (guessedType == null) return false;

        return true;
    }
}
