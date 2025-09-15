# 🌤️ SpringCloud Dashboard v3

**SpringCloud Dashboard v3** is a modern, cloud-based dashboard built with **Spring Boot** and **Thymeleaf**, designed to help users manage cloud files in a secure, responsive, and user-friendly environment. You can upload, preview, download, and delete files, with admin-level file management and improved performance.

---

## Table of Contents

1. [Features](#features)
2. [Technologies Used](#technologies-used)
3. [Installation](#installation)
4. [Usage](#usage)
5. [Contributing](#contributing)
6. [Reporting Issues](#reporting-issues)

---

## Features

- **User Authentication**: Secure login/logout with role-based access.
- **File Management**: Upload, preview, download, and delete files from the cloud.
- **Responsive UI**: Fully responsive layout for desktop, tablet, and mobile.
- **File Preview**: Preview images, videos, text files, and PDFs from the cloud.
- **Admin Panel**: Admins can view and manage all uploaded files.
- **Improved Performance**: Optimized backend and frontend for faster load times.
- **Enhanced Overlay**: Redesigned, intuitive overlay for easier navigation.
- **Install/Uninstall Scripts**: Simplified scripts for setup and removal.
- **No Port Required**: Access the dashboard via `http://YOUR_IP/springcloud/`.

---

## Technologies Used

- **Spring Boot** – Backend framework for REST APIs and serving frontend
- **Thymeleaf** – Template engine for dynamic HTML rendering
- **TailwindCSS** – Lightweight, responsive UI styling
- **HTML5 & CSS3** – Structure and styling of pages
- **JavaScript** – Client-side interactions and previews
- **Spring Security** – Authentication and role-based access
- **Java 17** – Runtime environment
- **Gradle** – Build automation and dependency management

---

## Installation

### Prerequisites

- **Java 17** (JDK 17 or newer)
- **Gradle** (for building the project)
- **MariaDB Database** (or another supported SQL database)
- **Shell/Terminal access** to run scripts

### Steps

1. **Download the latest release**:  
   Go to the [GitHub Releases page](https://github.com/jadnk/springcloud/releases) and download the latest `.jar` and `install.sh`.

2. **Run the install script**:  
   ./install.sh

3. **Access the dashboard**:  
   Open your browser at:  
   http://YOUR_IP/springcloud/

4. **Default credentials**:  
   - **Username**: `sysadmin`  
   - **Password**: Auto-generated in the console on startup

5. **Uninstalling**:  
   Run the provided `uninstall.sh` script for a clean removal.

---

## Usage

1. **Login**: Use your credentials to access the dashboard. Admins can access the admin panel.
2. **Upload Files**: Click the "+" button to upload new files.
3. **File List**: View files in a table with sortable columns for name, type, size, and upload date.
4. **Preview Files**: Click "Preview" to view file contents in the browser.
5. **Download/Delete**: Use the action buttons next to each file for download or deletion.

---

## Contributing

We welcome contributions!  

### How to Contribute

1. Fork the repository  
2. Create a new branch for your feature or fix:  
   feature/my-new-feature  
   bugfix/fix-issue
3. Implement changes with tests  
4. Submit a pull request to `main` with a detailed description

### Code of Conduct

- Be respectful and considerate  
- Follow the project’s code style  
- Document your changes clearly

### Credit the Original Author

If using or building on this project, please credit:  
Based on the [SpringCloud Dashboard](https://github.com/jadnk/springcloud) by [@verpxnter](https://github.com/jadnk)

---

## Reporting Issues

Encountered a bug or have a suggestion? Open an issue on GitHub with:  

- Detailed description of the problem  
- Steps to reproduce  
- Screenshots or logs if applicable

---

**Happy file managing!**  
— The SpringCloud Team
