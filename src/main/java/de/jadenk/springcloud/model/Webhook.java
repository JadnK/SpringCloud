package de.jadenk.springcloud.model;

import jakarta.persistence.*;

@Entity
@Table(name = "webhooks")
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "webhook_id")
    private Long id;

    @Column(name = "webhook_url", nullable = false)
    private String url;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "on_user_creation", nullable = false)
    private boolean onUserCreation = false;

    @Column(name = "on_user_ban", nullable = false)
    private boolean onUserBan = false;

    @Column(name = "on_register", nullable = false)
    private boolean onRegister = false;

    @Column(name = "on_error_thrown", nullable = false)
    private boolean onErrorThrown = false;

    @Column(name = "on_file_deletion", nullable = false)
    private boolean onFileDeletion = false;

    @Column(name = "on_file_upload", nullable = false)
    private boolean onFileUpload = false;

    @Column(name = "on_calendar_notification", nullable = false)
    private boolean onCalendarNotification = false;

    @Column(name = "on_user_update", nullable = false)
    private boolean onUserUpdate = false;

    @Column(name = "on_system_event", nullable = false)
    private boolean onSystemEvent = false;

    // Getter und Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOnUserCreation() {
        return onUserCreation;
    }

    public void setOnUserCreation(boolean onUserCreation) {
        this.onUserCreation = onUserCreation;
    }

    public boolean isOnUserBan() {
        return onUserBan;
    }

    public void setOnUserBan(boolean onUserBan) {
        this.onUserBan = onUserBan;
    }

    public boolean isOnRegister() {
        return onRegister;
    }

    public void setOnRegister(boolean onRegister) {
        this.onRegister = onRegister;
    }

    public boolean isOnErrorThrown() {
        return onErrorThrown;
    }

    public void setOnErrorThrown(boolean onErrorThrown) {
        this.onErrorThrown = onErrorThrown;
    }

    public boolean isOnFileDeletion() {
        return onFileDeletion;
    }

    public void setOnFileDeletion(boolean onFileDeletion) {
        this.onFileDeletion = onFileDeletion;
    }

    public boolean isOnFileUpload() {
        return onFileUpload;
    }

    public void setOnFileUpload(boolean onFileUpload) {
        this.onFileUpload = onFileUpload;
    }

    public boolean isOnCalendarNotification() {
        return onCalendarNotification;
    }

    public void setOnCalendarNotification(boolean onCalendarNotification) {
        this.onCalendarNotification = onCalendarNotification;
    }

    public boolean isOnUserUpdate() {
        return onUserUpdate;
    }

    public void setOnUserUpdate(boolean onUserUpdate) {
        this.onUserUpdate = onUserUpdate;
    }

    public boolean isOnSystemEvent() {
        return onSystemEvent;
    }

    public void setOnSystemEvent(boolean onSystemEvent) {
        this.onSystemEvent = onSystemEvent;
    }
}
