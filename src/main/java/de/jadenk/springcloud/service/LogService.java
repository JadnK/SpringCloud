package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.LogRepository;
import de.jadenk.springcloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private LogRepository logRepo;

    public void log(String username, String action) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Log log = new Log();
        log.setUser(user);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());

        logRepo.save(log);
    }
}
