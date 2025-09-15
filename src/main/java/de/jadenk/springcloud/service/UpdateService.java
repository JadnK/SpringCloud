package de.jadenk.springcloud.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Service
public class UpdateService {
    private final VersionService versionService;

    public UpdateService(VersionService versionService) {
        this.versionService = versionService;
    }

    public boolean isUpdateAvailable() {
        try {
            URL url = new URL("https://api.github.com/repos/Verpxnter/SpringCloud/releases/latest");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String response = in.lines().collect(Collectors.joining());
                JSONObject json = new JSONObject(response);
                String latest = json.getString("tag_name");

                return !latest.equals(versionService.getCurrentVersion());
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String fetchLatestVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/Verpxnter/SpringCloud/releases/latest");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String response = in.lines().collect(Collectors.joining());
                JSONObject json = new JSONObject(response);
                return json.getString("tag_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String checkForUpdateAndUpdateIfNeeded() {
        try {
            URL url = new URL("https://api.github.com/repos/JadnK/SpringCloud/releases/latest");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            JSONObject json = new JSONObject(response);
            String latest = json.getString("tag_name");

            if (latest.equals(versionService.getCurrentVersion())) {
                return "NO_UPDATE";
            }

            JSONArray assets = json.getJSONArray("assets");
            String jarUrl = null;
            String installScriptUrl = null;

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String name = asset.getString("name");
                if ("springcloud.jar".equals(name)) {
                    jarUrl = asset.getString("browser_download_url");
                } else if ("install.sh".equals(name)) {
                    installScriptUrl = asset.getString("browser_download_url");
                }
            }

            if (jarUrl == null) {
                return "ERROR_NO_JAR_ASSET";
            }
            if (installScriptUrl == null) {
                return "ERROR_NO_INSTALL_SCRIPT";
            }

            String jarDownloadPath = "/tmp/springcloud.jar";
            String installScriptDownloadPath = "/tmp/install.sh";

            downloadFile(jarUrl, jarDownloadPath);
            downloadFile(installScriptUrl, installScriptDownloadPath);

            makeExecutable(installScriptDownloadPath);

            runInstallScript(installScriptDownloadPath);

            return "SUCCESS_" + latest;

        } catch (Exception e) {
            return "ERROR_EXCEPTION_" + e.getMessage();
        }
    }


    private void downloadFile(String fileURL, String savePath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = httpConn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new IOException("Download fehlgeschlagen. HTTP Code: " + responseCode);
        }
        httpConn.disconnect();
    }

    private void makeExecutable(String filePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sudo", "chmod", "+x", filePath);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("chmod +x fehlgeschlagen mit Exit-Code " + exitCode);
        }
    }

    private void runInstallScript(String scriptPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sudo", "bash", scriptPath);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Fehler beim Ausführen des Installationsskripts: Exit-Code " + exitCode);
        }
    }
}
