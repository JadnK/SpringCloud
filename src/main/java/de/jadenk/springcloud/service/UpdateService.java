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

    public String checkForUpdateAndUpdateIfNeeded() {
        try {
            URL url = new URL("https://api.github.com/repos/Verpxnter/SpringCloud/releases/latest");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            JSONObject json = new JSONObject(response);
            String latest = json.getString("tag_name");

            if (latest.equals(versionService.getCurrentVersion())) {
                return "Version the newest";
            }

            JSONArray assets = json.getJSONArray("assets");
            String jarUrl = null;
            String installScriptUrl = null;

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String name = asset.getString("name");
                if (name.equals("springcloud.jar")) {
                    jarUrl = asset.getString("browser_download_url");
                } else if (name.equals("install.sh")) {
                    installScriptUrl = asset.getString("browser_download_url");
                }
            }

            if (jarUrl == null) {
                return "No springcloud.jar Asset gefunden";
            }
            if (installScriptUrl == null) {
                return "No install.sh Asset gefunden";
            }

            String jarDownloadPath = "/tmp/springcloud.jar";
            String installScriptDownloadPath = "/tmp/install.sh";

            downloadFile(jarUrl, jarDownloadPath);
            downloadFile(installScriptUrl, installScriptDownloadPath);

            makeExecutable(installScriptDownloadPath);

            runInstallScript(installScriptDownloadPath);

            return "Update auf Version " + latest + " erfolgreich durchgeführt";

        } catch (Exception e) {
            return "Fehler beim Update: " + e.getMessage();
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
        ProcessBuilder pb = new ProcessBuilder("chmod", "+x", filePath);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("chmod +x fehlgeschlagen mit Exit-Code " + exitCode);
        }
    }

    private void runInstallScript(String scriptPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("bash", scriptPath);
        pb.inheritIO(); // Ausgabe im Terminal sichtbar machen
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Fehler beim Ausführen des Installationsskripts: Exit-Code " + exitCode);
        }
    }
}
