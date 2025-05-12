# SpringCloud Dashboard

A simple and modern web-based dashboard application built using **Spring Boot** and **Thymeleaf**. This project allows users to manage their cloud files, upload, preview, download, and delete files in a secure and user-friendly environment.

---

## Table of Contents

1. [Features](#features)
2. [Technologies Used](#technologies-used)
3. [Installation](#installation)
4. [Usage](#usage)
5. [File Structure](#file-structure)

---

## Features

- **User Authentication**: Login and Logout functionality, with user-specific roles.
- **File Management**: Upload, preview, download, and delete files.
- **Responsive Design**: A fully responsive user interface that adapts to different screen sizes.
- **Previewing Files**: Support for previewing images, videos, text files, and PDFs.
- **Admin Panel**: Admin users can view and manage files uploaded by others.

---

## Technologies Used

- **Spring Boot**: Backend framework for building the RESTful API and serving the frontend.
- **Thymeleaf**: Template engine for rendering dynamic HTML pages.
- **HTML5 & CSS3**: For building the structure and styling the UI.
- **JavaScript**: For handling client-side interactions (like file preview and upload popup).
- **Spring Security**: For user authentication and role-based access control.
- **Java 17**: The project uses Java 17 as the runtime environment.
- **Gradle**: Build automation tool for managing project dependencies and building the project.

---

## Installation

To run this project locally, follow these steps:

### Prerequisites

- **Java 17** (JDK 17 or newer)
- **Gradle** (for building the project)
- **IDE** (like IntelliJ IDEA, Eclipse, etc.)
- Optional: **MariaDB-Datenbank** 

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/verpxnter/springcloud.git
   cd springcloud-dashboard
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Download the latest release from GitHub**:
   - Go to the [GitHub Releases page](https://github.com/verpxnter/springcloud/releases) and download the latest **.jar** file.
   
   **Important**: You should only download the project from the **Releases** section on GitHub, not from the source code directly, as the releases are pre-compiled and ready to run.

4. **Run the application**:

   Once you have the **.jar** file from the releases, you can start the application with the following command:

   ```bash
   java -jar springcloud.jar --url="jdbc:mariadb://YOUR IP:3306/YOUR DATABASE" --user="YOUR USERNAME" --pw="YOUR PASSWORD"
   ```

   This assumes you have an external MariaDB database. Update the database connection details as per your setup.

5. The application will run at `http://localhost:8080`. Open this URL in your browser to view the dashboard.

---

### Default Login Credentials

After starting the application, you can log in with the following **default admin user**:

- **Username**: `admin`
- **Password**: `jadenk_§!`

⚠️ Make sure to change the password in a production environment!

---

## Usage

1. **Login**: Enter your credentials to log into the dashboard. Admins can access the admin panel.
2. **Upload Files**: Click the "+" button at the bottom-right of the page to upload files.
3. **File List**: View uploaded files in a list with information like file name, type, and upload date.
4. **File Preview**: Click "Preview" next to a file to view its contents.
5. **Download/Delete Files**: Download or delete files by clicking the respective actions next to the file name.

---

## File Structure

The project follows a typical Spring Boot application structure.

```
springcloud-dashboard/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── de/
│   │   │       └── jadenk/
│   │   │           └── springcloud/
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── WebConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AdminController.java
│   │   │               │   ├── AuthenticationController.java
│   │   │               │   ├── BannedController.java
│   │   │               │   └── DashboardController.java
│   │   │               ├── exception/
│   │   │               │   └── ResourceNotFoundException.java
│   │   │               ├── model/
│   │   │               │   ├── Ban.java
│   │   │               │   ├── Log.java
│   │   │               │   ├── Role.java
│   │   │               │   ├── UploadedFile.java
│   │   │               │   └── User.java
│   │   │               ├── repository/
│   │   │               │   ├── LogRepository.java
│   │   │               │   ├── RoleRepository.java
│   │   │               │   ├── UploadedFileRepository.java
│   │   │               │   └── UserRepository.java
│   │   │               ├── security/
│   │   │               │   ├── BannedUserInterceptor.java
│   │   │               │   ├── CustomAuthenticationSuccessHandler.java
│   │   │               │   └── CustomUserDetails.java
│   │   │               └── service/
│   │   │               │   ├── CustomUserDetailsService.java
│   │   │               │   ├── FileUploadProgressListener.java
│   │   │               │   ├── FileUploadService.java
│   │   │               │   ├── LogService.java
│   │   │               │   └── UserService.java
│   │   │               ├── DatabaseInitializer.java
│   │   │               └── SpringcloudApplication.java
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   ├── admin.css
│   │   │   │   │   ├── banned.css
│   │   │   │   │   ├── dashboard.css
│   │   │   │   │   ├── login.css
│   │   │   │   │   └── register.css
│   │   │   ├── templates/
│   │   │   │   ├── admin.html
│   │   │   │   ├── banned.html
│   │   │   │   ├── dashboard.html
│   │   │   │   ├── login.html
│   │   │   │   └── register.html
│   │   │   ├── application.properties
├── build.gradle
└── README.md
```

- **`src/main/java/de/jadenk/springcloud/controller/`**: Contains the Spring MVC controllers that handle requests and responses.
- **`src/main/resources/static/css/`**: Contains the CSS files used for styling.
- **`src/main/resources/templates/`**: Contains the Thymeleaf templates (HTML files) that render the UI.
- **`src/main/resources/application.properties`**: Configuration file for Spring Boot.
- **`build.gradle`**: Gradle build configuration file.
