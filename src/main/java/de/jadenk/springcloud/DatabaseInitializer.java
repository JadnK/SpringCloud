package de.jadenk.springcloud;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.exception.CustomRuntimeException;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.CalendarWebhookScheduler;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Component("databaseInitializer")
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    @Lazy
    private CalendarWebhookScheduler calendarWebhookScheduler;


    @Override
    public void run(String... args) throws Exception {
        checkAndCreateTables();
        checkAndCreateDefaultRoles();
        createDefaultAdminUser();

        calendarWebhookScheduler.sendDailyCalendarReminders();
    }

    private void checkAndCreateTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            if (!tableExists(statement, "roles")) {
                createRolesTable(statement);
            }

            if (!tableExists(statement, "users")) {
                createUsersTable(statement);
            }

            if (!tableExists(statement, "folders")) {
                createFoldersTable(statement);
            }

            if (!tableExists(statement, "uploaded_files")) {
                createFilesTable(statement);
            } else {
                addFolderColumnToFiles(statement);
            }

            if (!tableExists(statement, "shared_links")) {
                createSharedLinks(statement);
            }

            if (!tableExists(statement, "logs")) {
                createLogsTable(statement);
            }

            if (!tableExists(statement, "calendar_entry")) {
                createCalendarTable(statement);
            }

            if (!tableExists(statement, "webhooks")) {
                createWebhookTable(statement);
            }

            if (!tableExists(statement, "file_authorizations")) {
                createFileAuthorizationsTable(statement);
            }

            if (!tableExists(statement, "api_token")) {
                createApiTokenTable(statement);
            }

            if (!tableExists(statement, "cloud_settings")) {
                createSiteSettingsTable(statement);
            }

        }
    }

    private void addFolderColumnToFiles(Statement statement) throws SQLException {
        try (var rs = statement.executeQuery("SHOW COLUMNS FROM uploaded_files LIKE 'folder_id'")) {
            if (!rs.next()) {
                statement.executeUpdate("ALTER TABLE uploaded_files " +
                        "ADD COLUMN folder_id BIGINT NULL, " +
                        "ADD CONSTRAINT fk_folder FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE;");
            }
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!§$%&/()=?@#";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    private void createDefaultAdminUser() {
        String newPassword = generateRandomPassword(12);
        String encodedPassword = securityConfig.passwordEncoder().encode(newPassword);

        Optional<User> existingUserOpt = userRepository.findByUsername("sysadmin");

        Role adminRole = roleRepository.findByName("SYSADMIN")
                .orElseThrow(() -> new CustomRuntimeException("[Database Initializer] SYSADMIN role not found."));

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setPassword(encodedPassword);
            userRepository.save(existingUser);
            System.out.println(" ");
            System.out.println("==============SYSADMIN-USER==============");
            System.out.println("Username: sysadmin");
            System.out.println("Password: " + newPassword);
            System.out.println("==============SYSADMIN-USER==============");
            System.out.println(" ");
        } else {
            User admin = new User();
            admin.setUsername("sysadmin");
            admin.setEmail("sysadmin@jadenk.de");
            admin.setPassword(encodedPassword);
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println(" ");
            System.out.println("==============SYSADMIN-USER==============");
            System.out.println("Username: sysadmin");
            System.out.println("Password: " + newPassword);
            System.out.println("==============SYSADMIN-USER==============");
            System.out.println(" ");
        }
    }



    private boolean tableExists(Statement statement, String tableName) throws SQLException {
        try (var resultSet = statement.executeQuery("SHOW TABLES LIKE '" + tableName + "'")) {
            return resultSet.next();
        }
    }

    private void createApiTokenTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE api_token (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255)," +
                "token VARCHAR(255) UNIQUE," +
                "active BOOLEAN" +
                ");");
    }


    private void createCalendarTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE calendar_entry (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "entry_date DATE NOT NULL," +
                "entry_time TIME," +
                "visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE'," +
                "user_id BIGINT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id));");
    }

    private void createSiteSettingsTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE cloud_settings (" +
                        "setting_key VARCHAR(255) PRIMARY KEY," +
                        "type VARCHAR(255)," +
                        "value TEXT);");
        statement.executeUpdate("INSERT INTO cloud_settings (setting_key, type, value) VALUES " +
                        "('IMGUR_CLIENT_ID', 'TEXT', NULL)," +
                        "('MAX_LOGIN_ATTEMPTS', 'NUMBER', 3)," +
                        "('API_RATE_LIMIT_PER_MINUTE', 'NUMBER', 40)," +
                        "('ALLOW_SHARING', 'CHECKBOX', 'true')," +
                        "('LANGUAGE', 'SELECT', 'de')," +
                        "('ENABLE_LOGGING', 'CHECKBOX', 'true');");
    }


    private void createWebhookTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE webhooks (" +
                "webhook_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "is_enabled BOOLEAN NOT NULL," +
                "webhook_url VARCHAR(255) NOT NULL," +
                "webhook_pic VARCHAR(255) NULL," +
                "webhook_name VARCHAR(255) NOT NULL," +
                "on_user_creation BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_user_ban BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_register BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_error_thrown BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_file_deletion BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_file_upload BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_calendar_notification BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_system_event BOOLEAN NOT NULL DEFAULT FALSE," +
                "on_user_update BOOLEAN NOT NULL DEFAULT FALSE" +
                ");");
    }

    private void createFileAuthorizationsTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE file_authorizations (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "file_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "FOREIGN KEY (file_id) REFERENCES uploaded_files(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");");
    }

    private void createFoldersTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE folders (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "owner_id BIGINT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");");
    }


    private void createUsersTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "role_id BIGINT, " +
                "username VARCHAR(255) NOT NULL UNIQUE, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "is_banned BOOLEAN DEFAULT FALSE, " +
                "profile_image_data LONGBLOB, " +
                "notifications_enabled BOOLEAN DEFAULT FALSE, " +
                "failed_login_attempts INT DEFAULT 0, " +
                "lockout_time DATETIME DEFAULT NULL, " +
                "FOREIGN KEY (role_id) REFERENCES roles(id)" +
                ");");
    }


    private void createSharedLinks(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE shared_links (" +
                "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "    user_id BIGINT NOT NULL," +
                "    file_id BIGINT NOT NULL," +
                "    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    expire_date TIMESTAMP NOT NULL," +
                "    token VARCHAR(255) UNIQUE NOT NULL," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)," +
                "    FOREIGN KEY (file_id) REFERENCES uploaded_files(id));");
    }

    private void createFilesTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE uploaded_files (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "file_name VARCHAR(255) NOT NULL," +
                "file_type VARCHAR(50) NOT NULL," +
                "file_data LONGBLOB NOT NULL," +
                "upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "file_owner_id BIGINT NOT NULL, " +
                "FOREIGN KEY (`file_owner_id`) REFERENCES `users`(`id`));");
    }

    private void createRolesTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE roles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL UNIQUE);");
    }

    private void createLogsTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE logs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT, " +
                "action VARCHAR(255), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id));");
    }

    private void checkAndCreateDefaultRoles() {
        if (roleRepository.count() == 0) {

            Role userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);

            Role devRole = new Role();
            devRole.setName("DEVELOPER");
            roleRepository.save(devRole);

            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);

            Role sysadminRole = new Role();
            sysadminRole.setName("SYSADMIN");
            roleRepository.save(sysadminRole);
        }
    }
}