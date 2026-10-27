# KEC ACADEMIX — Student Academic Evaluation & Management System

> **Institutional Academic Evaluation & Continuous Assessment Platform**  
> **Kongu Engineering College (Autonomous), Perundurai, Erode — 638060**  
> **Course:** 24ITT32 — Programming in Java Laboratory Project

---

### 👨‍🎓 Candidate & Project Details

| Parameter | Details |
| :--- | :--- |
| **Student Name** | **Vikneshwaran N** |
| **Roll Number** | **25ITR173** |
| **Degree & Branch** | B.Tech. Information Technology |
| **Regulation** | KEC Autonomous Regulations 2024 (R2024) |
| **Repository** | [github.com/VikneshwaranNv/Java_JDBC_project](https://github.com/VikneshwaranNv/Java_JDBC_project) |

---

## ⚡ Direct Downloads (One-Click Video & Document Access)

Click any of the cards or badges below to directly download the demonstration video or comprehensive evaluation documents:

| 🎬 Demonstration Video | 📄 Project Report (PDF) | 📝 Project Report (Word DOCX) |
| :---: | :---: | :---: |
| [![Download Video](https://img.shields.io/badge/▶_CLICK_TO_DOWNLOAD-DEMO_VIDEO_(25.7_MB)-red?style=for-the-badge)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Java-jdbc%20explantion%20video/video-jdbc_fE9iydgH.mp4) | [![Download Report PDF](https://img.shields.io/badge/⬇_CLICK_TO_DOWNLOAD-PROJECT_REPORT_(PDF)-red?style=for-the-badge)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Java_JDBC_PDF_Manual.pdf) | [![Download Report DOCX](https://img.shields.io/badge/⬇_CLICK_TO_DOWNLOAD-DOCUMENT_(DOCX)-0078d4?style=for-the-badge)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Final_Java_JDBC_Project_Report_Vikneshwaran_25ITR173.docx) |
| `video-jdbc_fE9iydgH.mp4`<br>*(Full walkthrough of CLI, GUI, and Database)* | `Java_JDBC_PDF_Manual.pdf`<br>*(Complete final documentation & calculation breakdown)* | `Final_Java_JDBC_Project_Report_Vikneshwaran_25ITR173.docx`<br>*(Editable Microsoft Word version)* |
| [🔗 Direct Raw Link](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Java-jdbc%20explantion%20video/video-jdbc_fE9iydgH.mp4) &nbsp;•&nbsp; [👁️ View Online](https://github.com/VikneshwaranNv/Java_JDBC_project/blob/main/Java-jdbc%20explantion%20video/video-jdbc_fE9iydgH.mp4) | [🔗 Direct Raw Link](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Java_JDBC_PDF_Manual.pdf) &nbsp;•&nbsp; [👁️ View Online](https://github.com/VikneshwaranNv/Java_JDBC_project/blob/main/Jdbc%20manual%20document/Java_JDBC_PDF_Manual.pdf) | [🔗 Direct Raw Link](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Final_Java_JDBC_Project_Report_Vikneshwaran_25ITR173.docx) |

> **Note:** Clicking the red **"CLICK TO DOWNLOAD"** button above triggers an immediate browser download of the high-definition MP4 video demonstration directly from GitHub.

---

## 📁 Repository Structure

```text
├── Java-Jdbc-project/             # Full application source code, libraries, SQL migrations & assets
│   ├── codes/                     # Java source files (KECGui, Main, EvaluationEngine, etc.)
│   ├── lib/                       # Dependency JARs (MySQL Connector, Apache PDFBox, jBCrypt)
│   ├── assets/                    # Official KEC crest branding
│   ├── migrations/                # Database SQL schemas
│   └── README.md                  # Local build & execution guide
├── Jdbc manual document/          # Project manual & lab reports (PDF & Word DOCX)
├── Java-jdbc explantion video/    # Video demonstration recording (video-jdbc_fE9iydgH.mp4)
├── .gitignore
└── README.md                      # Top-level documentation
```

---

## 🔌 What is JDBC (Java Database Connectivity)?

**JDBC (Java Database Connectivity)** is an industry-standard Java API (`java.sql` and `javax.sql`) providing universal, database-independent connectivity between Java applications and relational database engines (MySQL, PostgreSQL, Oracle).

### Core Components of JDBC
* **`DriverManager`:** Coordinates registered drivers and establishes live database sessions via connection URLs (`jdbc:mysql://127.0.0.1:3306/kec_academic`).
* **`Connection`:** Manages live database communication and transactional boundaries (`commit()`, `rollback()`, `setAutoCommit(false)`).
* **`PreparedStatement`:** Executes precompiled SQL queries with typed parameter binding (`?`), optimizing execution plans and completely neutralizing **SQL Injection**.
* **`ResultSet`:** A cursor-based tabular data structure that traverses query results and maps database records into Java objects.

> **Why JDBC in KEC ACADEMIX?** Guarantees zero-overhead direct SQL performance, atomic multi-table commit/rollback across marks and attendance, and deterministic schema constraint validation.

---

## 🛠️ Tech Stack & Tooling

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | **Java** | OpenJDK 21 LTS | Core OOP logic, modern switch expressions, multithreading, and file I/O. |
| **Desktop GUI** | **Java Swing & AWT** | Built-in JFC | Institutional desktop interface with Midnight Navy & Cream themes. |
| **Terminal CLI** | **Java Standard I/O** | `java.util.Scanner` | Interactive console interface for headless administration and grading tests. |
| **Database** | **MySQL Server** | 8.0 Community | ACID-compliant relational storage with foreign key cascade integrity. |
| **Database API** | **JDBC** | `java.sql.*` | Parameterized persistence and transactional operations. |
| **JDBC Driver** | **MySQL Connector/J** | `26.7.0` | Type-4 pure Java network driver over port 3306. |
| **Security** | **jBCrypt** | `0.4` | Salted password hashing (work factor 12) for role-based authentication. |
| **PDF Engine** | **Apache PDFBox** | `2.0.31` | Vector graphics rendering of official institutional semester marksheets. |

---

## 🏗️ Architecture & Workflow

KEC ACADEMIX follows a **Clean 3-Tier Layered Architecture**:

```mermaid
flowchart TD
    subgraph Presentation["Tier 1: Presentation Layer"]
        GUI["Desktop GUI (KECGui.java / KECTheme.java)"]
        CLI["Terminal Console (Main.java)"]
    end

    subgraph Business["Tier 2: Business Logic & Services"]
        AUTH["Auth Service & RBAC (AuthService.java / UserSession.java)"]
        EVAL["R2024 Evaluation Engine (EvaluationEngine.java)"]
        MARKS["Marks Management Service (MarksManagement.java)"]
        SAP["Activity Points Engine (SAPManagement.java)"]
    end

    subgraph DataTier["Tier 3: Persistence & Output"]
        JDBC["JDBC PreparedStatement & Transactions"]
        PDF["Apache PDFBox Engine (PdfMarksheetGenerator.java)"]
        DB[("MySQL Database (kec_academic)")]
        PDF_DOC["Official Marksheet PDF"]
    end

    GUI --> AUTH
    CLI --> AUTH
    GUI --> MARKS
    CLI --> MARKS
    GUI --> SAP

    MARKS --> EVAL
    AUTH --> JDBC
    MARKS --> JDBC
    SAP --> JDBC

    MARKS --> PDF
    PDF --> PDF_DOC
    JDBC <--> DB
```

### System Flow Summary
1. **Authentication:** User inputs credentials $\rightarrow$ `AuthService` verifies BCrypt salted hash against database $\rightarrow$ active `UserSession` is initialized with role-based permissions (Admin, Faculty, Student).
2. **Evaluation & Grading:** Faculty enters continuous assessment & ESE marks $\rightarrow$ `EvaluationEngine` validates attendance rules ($\ge$75% eligible, 65%–74.9% condonation, $<$65% detained), scales internal marks per course category, enforces minimum thresholds, and computes 10-point letter grades (`O` to `RA`).
3. **Marksheet Generation:** `PdfMarksheetGenerator` retrieves credit-weighted course grades, computes semester SGPA, and compiles a sealed institutional PDF with official crest vector layout via Apache PDFBox.

---

## 🗄️ Database Schema & Live Data Verification

The backend relational schema is built on **MySQL** and persisted using direct **JDBC** transactions. Below are the verified table definitions and live records from the database instance (`kec_academic`):

### 1. Course Table Schema (`DESCRIBE courses;`)

| Field | Type | Null | Key | Default | Extra |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `course_id` | `int` | NO | **PRI** | `NULL` | `auto_increment` |
| `course_code` | `varchar(20)` | NO | **UNI** | `NULL` | |
| `course_name` | `varchar(100)` | NO | | `NULL` | |
| `credits` | `int` | NO | | `NULL` | |
| `course_type` | `enum('THEORY','PRACTICAL','EMBEDDED')` | NO | | `NULL` | |

---

### 2. Live Database Records (`mysql> CLI Verification`)

#### 🎓 Students Table (`SELECT * FROM students;`)
```sql
+------------+---------+--------------------+------------------------+----------+
| student_id | roll_no | student_name       | department             | semester |
+------------+---------+--------------------+------------------------+----------+
|          1 | 24CSE001| Arun Kumar         | CSE                    |        4 |
|          2 | 24CSE002| Priya Devi         | CSE                    |        4 |
|          3 | 24IT001 | Karthik Raj        | IT                     |        4 |
|          4 | 25ITR173| VIKNESHWARAN N     | IT                     |        4 |
|          6 | 25ITR161| Thina Rajkumar R   | Information Technology |        4 |
+------------+---------+--------------------+------------------------+----------+
5 rows in set (0.00 sec)
```

#### 📚 Course Catalog (`SELECT * FROM courses;`)
```sql
+-----------+-------------+-----------------------------+---------+-------------+
| course_id | course_code | course_name                 | credits | course_type |
+-----------+-------------+-----------------------------+---------+-------------+
|         1 | CS2401      | Java Programming            |       4 | THEORY      |
|         2 | CS2402      | Database Management Systems |       3 | THEORY      |
|        15 | IT3448      | DBMS                        |       4 | THEORY      |
+-----------+-------------+-----------------------------+---------+-------------+
3 rows in set (0.00 sec)
```

#### 📋 Student Enrollments (`SELECT * FROM enrollments;`)
```sql
+---------------+------------+-----------+---------------+----------+
| enrollment_id | student_id | course_id | academic_year | semester |
+---------------+------------+-----------+---------------+----------+
|             1 |          1 |         1 | 2026-2027     |        4 |
|             2 |          1 |         2 | 2026-2027     |        4 |
|             3 |          2 |         1 | 2026-2027     |        4 |
|             4 |          2 |         2 | 2026-2027     |        4 |
|             5 |          3 |         1 | 2026-2027     |        4 |
|            18 |          4 |        15 | 2026-2027     |        3 |
+---------------+------------+-----------+---------------+----------+
6 rows in set (0.00 sec)
```

---

## 🚀 Quick Start (Compile & Run)

Navigate to the [`Java-Jdbc-project/`](Java-Jdbc-project/) folder:

```bash
cd Java-Jdbc-project
```

### 1. Compile Application
```bash
javac -cp "lib/*" -d build codes/*.java
```

### 2. Launch Swing Desktop GUI
```bash
java -cp "build;lib/*" KECGui
```

### 3. Launch Interactive Terminal Console (CLI)
```bash
java -cp "build;lib/*" Main
```

---

## 🔑 Demo Login Accounts

| Role | Username | Password | Responsibilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `test_admin_sys` | `AdminSecret2026!` | Course catalog setup, user management, system audit. |
| **Faculty / Staff** | `test_staff_kavitha` | `StaffSecret2026!` | CIA & ESE marks entry, condonation approval, SAP verification. |
| **Student** | `test_stud_arun` | `ValidStudentPass123!` | Roll No: `24CSE001` — View grades, upload certificates, download PDF marksheets. |

---

## 📑 Course Evaluation Models (KEC R2024 Regulations)

| Course Category | CIA Weight | ESE Weight | Minimum Passing Criteria |
| :--- | :---: | :---: | :--- |
| **Theory Courses** | **40%** | **60%** | $\ge$45% in ESE & $\ge$50% Aggregate |
| **Practical Courses** | **60%** | **40%** | $\ge$50% in ESE & $\ge$50% Aggregate |
| **Embedded Theory + Practical** | **50%** | **50%** | $\ge$45% in Theory ESE, $\ge$50% in Lab ESE & $\ge$50% Aggregate |

* **Attendance Requirements:** $\ge$75% Eligible to write ESE; 65%–74.9% requires Medical Condonation; $<$65% Detained (`SA`).
