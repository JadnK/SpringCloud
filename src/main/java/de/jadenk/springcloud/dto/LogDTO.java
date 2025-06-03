package de.jadenk.springcloud.dto;

import de.jadenk.springcloud.model.Log;

import java.time.LocalDateTime;

public class LogDTO {
    private Long id;
    private String action;
    private LocalDateTime timestamp;

    public LogDTO(Log log) {
        this.id = log.getId();
        this.action = log.getAction();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
