# KEC ACADEMIX — Student Academic Evaluation & Management System

**KEC ACADEMIX** is an institutional desktop and CLI application developed for **Kongu Engineering College (Autonomous)** using **Java 21**, **JDBC**, **MySQL**, and **Java Swing**. It provides an automated, role-governed platform for academic evaluation, continuous internal assessments, attendance tracking, and semester grading compliant with the **KEC R2024 Regulations**.

---

## 📁 Repository Structure

The repository is organized into three dedicated folders:

| Folder | Description | Quick Links |
| :--- | :--- | :--- |
| **[`Java-Jdbc-project/`](Java-Jdbc-project/)** | Full Java source code, libraries (`lib/`), migrations, UI assets, and configurations. | [Browse Source](Java-Jdbc-project/) |
| **[`Jdbc manual document/`](Jdbc%20manual%20document/)** | Official project manual, report, and laboratory record (`Java_JDBC_PDF_Manual.pdf`). | [⬇️ Direct Download PDF](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Java_JDBC_PDF_Manual.pdf) |
| **[`Java-jdbc explantion video/`](Java-jdbc%20explantion%20video/)** | HD walkthrough video demonstration of application functionality and JDBC operations. | [⬇️ Direct Download Video](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Java-jdbc%20explantion%20video/video-jdbc_fE9iydgH.mp4) |

---

## 📄 Project Documentation & Lab Manual

The comprehensive project report and laboratory manual for **24ITT32 - Programming in Java** by **Vikneshwaran N (25ITR173)** is located in the [`Jdbc manual document/`](Jdbc%20manual%20document/) folder:

* 📥 **[Download / View Project Manual (PDF)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Jdbc%20manual%20document/Java_JDBC_PDF_Manual.pdf)**
* 📁 **[Open Manual Folder](Jdbc%20manual%20document/)**

---

## 🎬 Video Demonstration

A detailed walkthrough video covering database setup, login authentication, mark entry, R2024 SGPA grading, and PDF marksheet export is located in the [`Java-jdbc explantion video/`](Java-jdbc%20explantion%20video/) folder:

* 📥 **[Direct Download Video (video-jdbc_fE9iydgH.mp4)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/Java-jdbc%20explantion%20video/video-jdbc_fE9iydgH.mp4)**
* 📁 **[Open Video Folder](Java-jdbc%20explantion%20video/)**

---

## 🚀 Quick Start (Running the Application)

To run the application, navigate into the [`Java-Jdbc-project/`](Java-Jdbc-project/) directory:

```bash
cd Java-Jdbc-project
```

### 1. Compile Source Code
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

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `test_admin_sys` | `AdminSecret2026!` | Full curriculum, faculty, student & system privileges |
| **Faculty / Staff** | `test_staff_kavitha` | `StaffSecret2026!` | Marks entry, SAP approvals & attendance management |
| **Student** | `test_stud_arun` | `ValidStudentPass123!` | Roll No: `24CSE001`, marksheet download & SAP submission |

---

## 🛠️ Key Capabilities & R2024 Features

* **Curriculum & Assessment Models:** Full support for Theory (40/60), Practical (60/40), and Embedded (50/50) grading schemas.
* **Attendance & Medical Condonation:** Strict enforcement of attendance criteria ($\ge$75% Eligible, 65%–74.9% Medical Condonation Workflow, and $<$65% Detention / SA).
* **Automated R2024 Evaluation:** Real-time continuous internal marks scaling, minimum ESE score checks, 10-point letter grades (`O` to `RA`), and credit-weighted SGPA calculation.
* **Official Marksheet Generation:** Automated generation of institutional PDF marksheets with college seal and course breakdown using Apache PDFBox.
* **Role-Based Security:** BCrypt password hashing and session governance across Admin, Faculty, and Student tiers.
