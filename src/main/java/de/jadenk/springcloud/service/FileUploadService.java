package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Folder;
import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.repository.WebhookRepository;
import de.jadenk.springcloud.util.MessageService;
import de.jadenk.springcloud.util.WebhookEvent;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;
    @Autowired
    private WebhookService webhookService;


    public void uploadFile(MultipartFile file, User owner, Folder folder) throws IOException {

        FileUploadProgressListener progressListener = new FileUploadProgressListener(file);

        byte[] fileBytes = file.getBytes();
        for (int i = 0; i < fileBytes.length; i++) {
            progressListener.updateProgress(i);
        }

        UploadedFile uploadedFile = new UploadedFile(
                file.getOriginalFilename(),
                file.getContentType(),
                fileBytes,
                owner
        );

        uploadedFile.setFolder(folder);  // ← Ordner setzen

        uploadedFileRepository.save(uploadedFile);

        String message = messageService.getLog("dashboard.file.upload", file.getOriginalFilename());
        Log log = logService.log(owner.getUsername(), message);

        webhookService.triggerWebhookEvent(WebhookEvent.FILE_UPLOADED,
                "User " + owner.getUsername() + " uploaded a file", log.getId());
    }

}
