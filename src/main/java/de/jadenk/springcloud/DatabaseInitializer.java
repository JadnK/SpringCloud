package de.jadenk.springcloud;

import de.jadenk.springcloud.config.SecurityConfig;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;


    @Override
    public void run(String... args) throws Exception {
        checkAndCreateTables();
        checkAndCreateDefaultRoles();
        createDefaultAdminUser();
    }

    private void checkAndCreateTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            if (!tableExists(statement, "users")) {
                createUsersTable(statement);
            }

            if (!tableExists(statement, "roles")) {
                createRolesTable(statement);
            }

            if (!tableExists(statement, "user_roles")) {
                createUserRolesTable(statement);
            }

            if (!tableExists(statement, "logs")) {
                createLogsTable(statement);
            }

            if (!tableExists(statement, "uploaded_files")) {
                createFilesTable(statement);
            }
        }
    }

    private void createDefaultAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@jadenk.de");
            admin.setPassword(securityConfig.passwordEncoder().encode("jadenk_§!"));

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            admin.getRole().add(adminRole);

            userRepository.save(admin);
            System.out.println("Default admin user created.");
        }
    }


    private boolean tableExists(Statement statement, String tableName) throws SQLException {
        try (var resultSet = statement.executeQuery("SHOW TABLES LIKE '" + tableName + "'")) {
            return resultSet.next();
        }
    }

    private void createUsersTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL UNIQUE, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL)," +
                "is_banned BOOLEAN DEFAULT FALSE);");
    }

    private void createFilesTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE uploaded_files (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "file_name VARCHAR(255) NOT NULL," +
                "file_type VARCHAR(50) NOT NULL," +
                "file_data LONGBLOB NOT NULL," +
                "upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "file_owner_id INT NOT NULL, " +
                "FOREIGN KEY (`file_owner_id`) REFERENCES `users`(`id`));");
    }

    private void createRolesTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE roles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL UNIQUE);");
    }

    private void createUserRolesTable(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE user_roles (" +
                "user_id BIGINT, " +
                "role_id BIGINT, " +
                "PRIMARY KEY (user_id, role_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (role_id) REFERENCES roles(id));");
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
        }
    }
}