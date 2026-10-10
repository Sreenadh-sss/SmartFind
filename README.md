# SmartFind – AI-Based Smart Lost & Found System

SmartFind is a full-stack college capstone web application designed for campus communities to report, search, and automatically match lost and found belongings using a transparent multi-factor weighted matching algorithm.

---

## 🛠️ Technology Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Spring Boot 3.4.x | Java 21/24 compatible, Maven build |
| **REST APIs** | Spring Web | JSON response formatting, CORS enabled |
| **ORM / Database** | Spring Data JPA & MySQL | Entity mapping, transactional operations |
| **Security & Auth** | BCrypt Password Hashing | Secure password storage, role validation (`USER`, `ADMIN`) |
| **Validation** | Jakarta Bean Validation | Robust request validation with friendly errors |
| **Frontend** | Vanilla HTML5, CSS3, JS | Responsive dashboard, VS Code Live Server compatible |

---

## 📁 Project Architecture & Folder Structure

```text
SmartLostFound project/
├── SmartFindBackend/                  <-- Import into Spring Tools Suite (STS) as Existing Maven Project
│   ├── pom.xml
│   ├── schema.sql                     <-- Optional manual database script
│   └── src/
│       ├── main/
│       │   ├── java/com/smartfind/
│       │   │   ├── SmartFindApplication.java
│       │   │   ├── config/            <-- CorsConfig, DatabaseInitializer, SecurityConfig
│       │   │   ├── controller/        <-- AuthController, ItemReportController, MatchingController, AdminController
│       │   │   ├── dto/               <-- ApiResponse, AuthResponse, ItemRequest, MatchResultDto, etc.
│       │   │   ├── entity/            <-- User, ItemReport, ReportType, ItemStatus
│       │   │   ├── exception/         <-- GlobalExceptionHandler, BadRequestException, etc.
│       │   │   ├── repository/        <-- UserRepository, ItemReportRepository
│       │   │   └── service/           <-- UserService, ItemReportService, MatchingService
│       │   └── resources/
│       │       └── application.properties
│       └── test/java/com/smartfind/
│           └── MatchingServiceTest.java
│
└── SmartFindFrontend/                 <-- Open in Visual Studio Code (Run with Live Server)
    ├── index.html                     <-- Dashboard with real-time stats & AI matching panel
    ├── login.html                     <-- User & Admin login
    ├── register.html                  <-- Account registration
    ├── lost-item.html                 <-- Report lost item form
    ├── found-item.html                <-- Report found item form
    ├── search.html                    <-- Dynamic search and filtering
    ├── my-reports.html                <-- User submitted reports management & deletion
    ├── admin.html                     <-- Moderation panel for ADMIN role
    ├── style.css                      <-- Responsive modern design system
    └── script.js                      <-- API integration, state management & UI rendering
```

---

## ⚙️ Step 1: Database Setup (MySQL)

1. Ensure MySQL server is running (port `3306`).
2. Open **MySQL Workbench** or command line and execute:
   ```sql
   CREATE DATABASE IF NOT EXISTS smartfind_db;
   ```
   *(Note: Hibernate will automatically create and update the `users` and `item_reports` tables).*
3. Update database credentials if needed in:
   `SmartFindBackend/src/main/resources/application.properties`
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/smartfind_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root   # <-- Change this to your MySQL root password
   ```
   *(Alternatively, you can set system environment variables `DB_USERNAME` and `DB_PASSWORD`).*

---

## 🚀 Step 2: Running Backend in Spring Tools Suite (STS)

1. Open **Spring Tools Suite (STS)**.
2. Select **File** -> **Import...** -> **Maven** -> **Existing Maven Projects**.
3. Click **Browse...** and select the folder `SmartFindBackend` inside your workspace.
4. Click **Finish**. Wait for Maven dependencies to complete indexing.
5. In the **Package Explorer**, locate:
   `src/main/java/com/smartfind/SmartFindApplication.java`
6. Right-click `SmartFindApplication.java` -> **Run As** -> **Spring Boot App**.
7. The console will display:
   ```text
   SmartFind Backend is running successfully at http://localhost:8083
   Database: MySQL (smartfind_db) | Port: 8083
   Default Admin: admin@smartfind.com / admin123
   Default Student: student@smartfind.com / student123
   ```

---

## 🌐 Step 3: Running Frontend in Visual Studio Code

1. Open **Visual Studio Code**.
2. Open the folder:
   `c:\Users\K.SREENADH\Downloads\SmartLostFound project\SmartFindFrontend`
3. Ensure the extension **Live Server** (by *Ritwick Dey*) is installed in VS Code.
4. Right-click `index.html` (or `login.html`) -> click **Open with Live Server**.
5. Live Server will open your browser at `http://127.0.0.1:5500` (or `http://127.0.0.1:5501`).
6. CORS is pre-configured on the backend to accept all local requests from Live Server.

---

## 🤖 Step 4: AI-Based Matching Engine Breakdown

The system implements a transparent multi-factor weighted scoring algorithm:

| Factor | Weight | Scoring Logic |
| :--- | :--- | :--- |
| **Item Name Similarity** | **30%** | Combines normalized token overlap, substring containment, and Levenshtein edit distance. |
| **Description Keywords** | **30%** | Strips common stop-words and computes keyword intersection/Jaccard containment. |
| **Location Similarity** | **25%** | Matches campus buildings, room numbers, and common landmark keywords. |
| **Date Proximity** | **15%** | Evaluates difference in days between lost date and found date (0 days = 100%, ≤2 days = 85%, etc.). |

- Matches are returned if the combined score is **≥ 60.0%**.
- A detailed breakdown explanation is provided for every match.
