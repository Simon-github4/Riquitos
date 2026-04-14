package com.riquitos.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/riquitoserp}")
    private String dbUrl;

    @Value("${spring.datasource.username:postgres}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.datasource.pgdump-path:pg_dump}")
    private String pgDumpPath;

    @Value("${spring.datasource.pgrestore-path:pg_restore}") 
    private String pgRestorePath;
    
    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${app.backup.max-backups:10}")
    private int maxBackups;

    @Value("${app.backup.auto-enabled:true}")
    private boolean autoBackupEnabled;

    @Value("${dropbox.refresh-token:}")
    private String dropboxRefreshToken;

    @Value("${dropbox.app-key:}")
    private String dropboxAppKey;

    @Value("${dropbox.app-secret:}")
    private String dropboxAppSecret;

    @Scheduled(fixedRate = 86400000)
    public void scheduledBackup() {
        if (!autoBackupEnabled) {
            return;
        }
        logger.info("Iniciando backup automático...");
        try {
            String backupPath = createBackup();
            logger.info("Backup local completado: {}", backupPath);
            
            uploadToDropbox(backupPath, "BACKUP_Riquitos.sql");
            
        } catch (Exception e) {
            logger.error("Error en backup automático: {}", e.getMessage(), e);
        }
    }

    public String createBackup() throws IOException, InterruptedException {
        String dbName = extractDbName(dbUrl);
        Path backupDir = Paths.get(backupDirectory);
        Files.createDirectories(backupDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss"));
        String backupFileName = "backup_riquitos_" + timestamp + ".sql";
        Path backupFilePath = backupDir.resolve(backupFileName);

        List<String> command = new ArrayList<>();
        command.add(pgDumpPath);
        command.add("-h"); command.add(extractHost(dbUrl));
        command.add("-p"); command.add(extractPort(dbUrl));
        command.add("-U"); command.add(dbUsername);
        command.add("-d"); command.add(dbName);
        command.add("-f"); command.add(backupFilePath.toString());
        command.add("--clean");
        command.add("--if-exists");
        command.add("-Fc");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("PGPASSWORD", dbPassword);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("[pg_dump] {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Backup local falló con código: " + exitCode);
        }

        cleanupOldBackups();
        return backupFilePath.toString();
    }

    private void uploadToDropbox(String filePath, String fileName) {
        if (dropboxRefreshToken.isEmpty() || dropboxAppKey.isEmpty() || dropboxAppSecret.isEmpty()) {
            logger.warn("Saltando subida a Dropbox: Faltan credenciales en la configuración.");
            return;
        }

        DbxRequestConfig config = DbxRequestConfig.newBuilder("riquitos-backup-app")
            .withHttpRequestor(new OkHttp3Requestor(new okhttp3.OkHttpClient()))
            .build();

        DbxCredential credential = new DbxCredential("", 0L, dropboxRefreshToken, dropboxAppKey, dropboxAppSecret);
        DbxClientV2 client = new DbxClientV2(config, credential);
        
        try (InputStream in = new FileInputStream(filePath)) {
            logger.info("Iniciando subida a Dropbox: {}", fileName);
            
            FileMetadata metadata = client.files().uploadBuilder("/" + fileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);
            
            logger.info("Backup subido a Dropbox con éxito: {}", metadata.getPathDisplay());

        } catch (Exception e) {
            logger.error("Error al subir a Dropbox: {}", e.getMessage());
        }
    }

    public List<BackupInfo> listBackups() throws IOException {
        Path backupDir = Paths.get(backupDirectory);
        if (!Files.exists(backupDir)) {
            return new ArrayList<>();
        }

        return Files.list(backupDir)
            .filter(path -> path.toString().endsWith(".sql") || path.toString().endsWith(".dump"))
            .map(path -> {
                try {
                    File file = path.toFile();
                    return new BackupInfo(
                        file.getName(),
                        path.toString(),
                        Files.getLastModifiedTime(path).toMillis(),
                        file.length()
                    );
                } catch (IOException e) {
                    return null;
                }
            })
            .filter(info -> info != null)
            .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
            .collect(Collectors.toList());
    }

    public boolean restoreBackup(String backupPath) throws IOException, InterruptedException {
        String dbName = extractDbName(dbUrl);

        List<String> command = new ArrayList<>();
        command.add(pgRestorePath);
        command.add("-h");
        command.add(extractHost(dbUrl));
        command.add("-p");
        command.add(extractPort(dbUrl));
        command.add("-U");
        command.add(dbUsername);
        command.add("-d");
        command.add(dbName);
        command.add("--clean");
        command.add("--if-exists");
        command.add("-Fc");
        command.add(backupPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("PGPASSWORD", dbPassword);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[pg_restore] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Restore failed with exit code: " + exitCode);
        }
        return true;
    }

    public void deleteBackup(String backupPath) throws IOException {
        Files.delete(Paths.get(backupPath));
    }

    private void cleanupOldBackups() throws IOException {
        List<BackupInfo> backups = listBackups();
        if (backups.size() > maxBackups) {
            for (int i = maxBackups; i < backups.size(); i++) {
                deleteBackup(backups.get(i).getPath());
            }
        }
    }

    private String extractDbName(String url) {
        int lastSlash = url.lastIndexOf('/');
        int questionMark = url.indexOf('?', lastSlash);
        if (questionMark > 0) {
            return url.substring(lastSlash + 1, questionMark);
        }
        return url.substring(lastSlash + 1);
    }

    private String extractHost(String url) {
        int start = url.indexOf("://") + 3;
        int end = url.indexOf(":", start);
        if (end < 0) {
            end = url.indexOf("/", start);
        }
        return url.substring(start, end > 0 ? end : url.length());
    }

    private String extractPort(String url) {
        int colon = url.indexOf(":", url.indexOf("://") + 3);
        if (colon < 0) {
            return "5432";
        }
        int slash = url.indexOf("/", colon);
        return url.substring(colon + 1, slash > 0 ? slash : url.length());
    }

    public static class BackupInfo {
        private final String name;
        private final String path;
        private final long timestamp;
        private final long size;

        public BackupInfo(String name, String path, long timestamp, long size) {
            this.name = name;
            this.path = path;
            this.timestamp = timestamp;
            this.size = size;
        }

        public String getName() { return name; }
        public String getPath() { return path; }
        public long getTimestamp() { return timestamp; }
        public long getSize() { return size; }

        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
