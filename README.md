# Aliyun Cost Management System - Java Version

This is a Java/Spring Boot refactoring of the original Python Flask application for managing Aliyun (Alibaba Cloud) costs and bills.

## Features

- **User Authentication**: Login, Register, Logout with session management
- **User Management**: Admin can view all users, toggle status, reset passwords
- **Aliyun Key Management**: Add, delete, view Aliyun Access Keys with AES-256 encryption
- **Bill Management**: Fetch bills from Aliyun API, view bills with filtering, pagination, delete bills
- **Bill Export**: Export bills to Excel format
- **Operation Logs**: View all operation logs with filtering

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Template Engine**: Thymeleaf
- **Database**: SQLite (with Hibernate JPA)
- **Security**: Spring Security with BCrypt password encoding
- **Excel Export**: Apache POI
- **Build Tool**: Maven

## Project Structure

```
src/main/
├── java/com/example/mywebsite/
│   ├── MyWebsiteApplication.java      # Main application entry point
│   ├── config/
│   │   └── SecurityConfig.java        # Spring Security configuration
│   ├── controller/
│   │   ├── AuthController.java        # Authentication controllers
│   │   ├── AdminController.java       # Admin management controllers
│   │   └── MainController.java        # Main application controllers
│   ├── entity/
│   │   ├── User.java                  # User entity
│   │   ├── AliyunKey.java             # Aliyun Access Key entity
│   │   ├── Bill.java                  # Bill entity
│   │   └── OperationLog.java          # Operation log entity
│   ├── repository/
│   │   ├── UserRepository.java        # User data access
│   │   ├── AliyunKeyRepository.java   # Key data access
│   │   ├── BillRepository.java        # Bill data access
│   │   └── OperationLogRepository.java# Log data access
│   ├── service/
│   │   ├── UserService.java           # User business logic
│   │   ├── AliyunKeyService.java      # Key business logic
│   │   ├── BillService.java           # Bill business logic
│   │   ├── AliyunApiService.java      # Aliyun API integration
│   │   ├── EncryptionService.java     # AES encryption service
│   │   └── OperationLogService.java   # Logging service
│   └── dto/
│       └── ApiResponse.java           # API response wrapper
└── resources/
    ├── application.properties         # Application configuration
    ├── static/
    │   └── css/
    │       └── style.css              # Global styles
    └── templates/                     # Thymeleaf templates
        ├── home.html
        ├── login.html
        ├── register.html
        ├── aliyun_keys.html
        ├── aliyun_bills.html
        ├── admin_users.html
        └── admin_logs.html
```

## Build and Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Building
```bash
mvn clean package
```

### Running
```bash
java -jar target/my-website-1.0.0.jar
```

The application will start at http://localhost:5000

### Default Credentials
- Username: admin
- Password: admin

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
server.port=5000

# Database Configuration (SQLite)
spring.datasource.url=jdbc:sqlite:users.db

# Application Custom Configuration
app.secret-key=set_a_fixed_secret_key_here_please_change_it
```

## Notes

- The AliyunApiService currently uses mock data. For production use, integrate with the actual Aliyun SDK.
- The database (users.db) will be automatically created on first run.
- Default admin account is created automatically if it doesn't exist.
