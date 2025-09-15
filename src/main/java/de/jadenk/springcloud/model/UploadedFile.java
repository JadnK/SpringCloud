package de.jadenk.springcloud.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Lob
    private byte[] fileData;

    private LocalDateTime uploadTime;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedLink> sharedLinks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "file_authorizations",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> authorizedUsers = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "file_owner_id", referencedColumnName = "id")
    private User fileOwner;

    public UploadedFile() {}

    public UploadedFile(String fileName, String fileType, byte[] fileData, User fileOwner) {
        this.fileData = fileData;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadTime = LocalDateTime.now();
        this.fileOwner = fileOwner;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public User getFileOwner() {
        return fileOwner;
    }

    public void setFileOwner(User fileOwner) {
        this.fileOwner = fileOwner;
    }

    public List<User> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(List<User> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
