# SpringCloud Dashboard

A simple and modern web-based dashboard application built using **Spring Boot** and **Thymeleaf**. This project allows users to manage their cloud files, upload, preview, download, and delete files in a secure and user-friendly environment.

---

## Table of Contents

1. [Features](#features)
2. [Technologies Used](#technologies-used)
3. [Installation](#installation)
4. [Usage](#usage)
5. [Contributing](#contributing)

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
- **MariaDB-Datenbank** (Local Storage is Comming Soon)

### Steps

1. **Download the latest release from GitHub**:
   - Go to the [GitHub Releases page](https://github.com/verpxnter/springcloud/releases) and download the latest **.jar** and **install.sh** file.
   
   **Important**: You should only download the project from the **Releases** section on GitHub, not from the source code directly, as the releases are pre-compiled and ready to run.

2. **Run the application**:

   Once you have the **.sh** file from the releases, you can start the application with the following command:

   ```bash
   ./install.sh
   ```

   This assumes you have an external MariaDB database. Update the database connection details as per your setup.

5. The application will run at `https://localhost:8443`. Open this URL in your browser to view the dashboard.

---

### Default Login Credentials

After starting the application, you can log in with the following **default sysadmin user**:

- **Username**: `sysadmin`
- **Password**: Random generated in Console on every Boot.

---

## Usage

1. **Login**: Enter your credentials to log into the dashboard. Admins can access the admin panel.
2. **Upload Files**: Click the "+" button at the bottom-right of the page to upload files.
3. **File List**: View uploaded files in a list with information like file name, type, and upload date.
4. **File Preview**: Click "Preview" next to a file to view its contents.
5. **Download/Delete Files**: Download or delete files by clicking the respective actions next to the file name.

---

## Contributing

We welcome contributions to the SpringCloud Dashboard project! If you'd like to contribute, please follow the guidelines below.

### How to Contribute

1. **Fork the repository**: Start by forking the repository to your own GitHub account.
2. **Create a new branch**: Always create a new branch for each feature or bug fix you are working on. Use descriptive names for your branches (e.g., `feature/upload-improvement` or `bugfix/missing-icon`).
3. **Write tests**: Ensure that any changes you make are covered by appropriate tests.
4. **Submit a pull request**: Once you're done with your changes, submit a pull request to the `main` branch. Make sure to describe your changes in detail.

### Code of Conduct

- Be respectful and considerate of others.
- Follow the existing code style and practices of the project.
- Submit clear, well-documented code with a detailed description of your changes.

### How to Mention or Credit Me

If you're using or building upon this project, I’d appreciate it if you could credit me as the original creator. A simple acknowledgment in your repository or project would be great! Here’s an example of how you can do it:

- **In your `README.md`**:
   ```markdown
   Based on the [SpringCloud Dashboard](https://github.com/verpxnter/springcloud) by [@verpxnter](https://github.com/verpxnter).
   ```

## Reporting Issues

If you encounter any bugs or have suggestions for improvements, feel free to open an issue on the GitHub repository. Please provide a detailed description of the problem and, if applicable, steps to reproduce.
