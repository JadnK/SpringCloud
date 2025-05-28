package de.jadenk.springcloud.dto;

import de.jadenk.springcloud.model.User;

import java.util.Base64;

public class UserDTO {
    private Long id;
    private String username;
    private String profileImageBase64;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();

        if (user.getProfileImageData() != null) {
            this.profileImageBase64 = Base64.getEncoder().encodeToString(user.getProfileImageData());
        } else {
            this.profileImageBase64 = null;
        }
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getProfileImageBase64() { return profileImageBase64; }
}
