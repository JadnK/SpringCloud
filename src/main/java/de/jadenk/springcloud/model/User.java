package de.jadenk.springcloud.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(name = "is_banned")
    private boolean banned;

    @Column(unique = true)
    private String email;

    @Transient
    private int failedLoginAttempts = 0;
    @Transient
    private LocalDateTime lockoutTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private Set<Log> logs;

    @Lob
    @Column(name = "profile_image_data")
    private byte[] profileImageData;

    @Column(name = "notifications_enabled")
    private boolean notificationsEnabled;

    public byte[] getProfileImageData() {
        return profileImageData;
    }

    public void setProfileImageData(byte[] profileImageData) {
        this.profileImageData = profileImageData;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }


    public Long getId() {
        return id;
    }


    public Set<Log> getLogs() {
        return logs;
    }

    public void setLogs(Set<Log> logs) {
        this.logs = logs;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int attempts) {
        failedLoginAttempts=attempts;
    }

    public LocalDateTime getLockoutTime() {
        return lockoutTime;
    }

    public void setLockoutTime(LocalDateTime lockoutTime) {
        this.lockoutTime = lockoutTime;
    }
}