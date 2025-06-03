package de.jadenk.springcloud.dto;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;

import java.util.Base64;
import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private String profileImageBase64;
    private String mail;
    private Role role;
    private Set<Log> logs;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();

        if (user.getProfileImageData() != null) {
            this.profileImageBase64 = Base64.getEncoder().encodeToString(user.getProfileImageData());
        } else {
            this.profileImageBase64 = null;
        }
        this.mail=user.getEmail();
        this.role=user.getRole();
        this.logs=user.getLogs();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getProfileImageBase64() { return profileImageBase64; }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Log> getLogs() {
        return logs;
    }

    public void setLogs(Set<Log> logs) {
        this.logs = logs;
    }
}
