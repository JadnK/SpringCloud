package de.jadenk.springcloud.service;

import org.springframework.stereotype.Service;

@Service
public class VersionService {
    private final String currentVersion = "2.1";

    public String getCurrentVersion() {
        return currentVersion;
    }
}
