package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.UploadedFile;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.UploadedFileRepository;
import de.jadenk.springcloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileUploadService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogService logService;

    public void uploadFile(MultipartFile file) throws IOException {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);

        UploadedFile uploadedFile = new UploadedFile(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                user
        );
        uploadedFileRepository.save(uploadedFile);

        logService.log(currentUser.getUsername(), "File Uploaded: " + file.getOriginalFilename());
    }
}
