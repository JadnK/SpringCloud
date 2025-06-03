package de.jadenk.springcloud.dto;

import de.jadenk.springcloud.model.UploadedFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Base64;

public class UploadedFileDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileDataBase64;
    private LocalDateTime uploadTime;

    private Long fileOwnerId;
    private String fileOwnerUsername;

    private List<Long> authorizedUserIds;
    private List<String> authorizedUsernames;

    public UploadedFileDTO(UploadedFile file) {
        this.id = file.getId();
        this.fileName = file.getFileName();
        this.fileType = file.getFileType();
        this.uploadTime = file.getUploadTime();

        if (file.getFileData() != null) {
            this.fileDataBase64 = Base64.getEncoder().encodeToString(file.getFileData());
        } else {
            this.fileDataBase64 = null;
        }

        if (file.getFileOwner() != null) {
            this.fileOwnerId = file.getFileOwner().getId();
            this.fileOwnerUsername = file.getFileOwner().getUsername();
        }

        if (file.getAuthorizedUsers() != null) {
            this.authorizedUserIds = file.getAuthorizedUsers().stream()
                    .map(u -> u.getId())
                    .collect(Collectors.toList());

            this.authorizedUsernames = file.getAuthorizedUsers().stream()
                    .map(u -> u.getUsername())
                    .collect(Collectors.toList());
        }
    }

    // Getter (Setters kannst du hinzufügen, falls nötig)

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileDataBase64() {
        return fileDataBase64;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public Long getFileOwnerId() {
        return fileOwnerId;
    }

    public String getFileOwnerUsername() {
        return fileOwnerUsername;
    }

    public List<Long> getAuthorizedUserIds() {
        return authorizedUserIds;
    }

    public List<String> getAuthorizedUsernames() {
        return authorizedUsernames;
    }
}
