package de.jadenk.springcloud.service;

import org.apache.tomcat.util.http.fileupload.ProgressListener;
import org.springframework.web.multipart.MultipartFile;


public class FileUploadProgressListener {

    private long totalBytes;
    private long bytesRead;
    private double progress;

    public FileUploadProgressListener(MultipartFile file) {
        this.totalBytes = file.getSize();
        this.bytesRead = 0;
        this.progress = 0.0;
    }

    public void updateProgress(long bytesRead) {
        this.bytesRead = bytesRead;
        this.progress = (double) bytesRead / totalBytes * 100;

        System.out.println("Uploading Progress: " + (bytesRead * 100 / totalBytes) + "%");
    }

    public double getProgress() {
        return progress;
    }
}
