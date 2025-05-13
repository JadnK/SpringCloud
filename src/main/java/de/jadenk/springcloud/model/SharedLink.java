package de.jadenk.springcloud.model;

import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_links")
public class SharedLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private LocalDateTime expireDate;

    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private UploadedFile file;

    public SharedLink(UploadedFile file, User user, String token, LocalDateTime expireDate) {
        this.token = token;
        this.expireDate = expireDate;
        this.user = user;
        this.file = file;
    }

    public SharedLink() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }
}
