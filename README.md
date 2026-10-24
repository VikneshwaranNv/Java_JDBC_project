# KEC ACADEMIX — Student Academic Evaluation & Management System

**KEC ACADEMIX** is an institutional desktop application built for **Kongu Engineering College (Autonomous)**. Developed with **Java 21**, **Java Swing**, and **MySQL**, it provides an automated, role-governed platform for academic evaluation, continuous internal assessments, attendance tracking, and semester grading compliant with the **KEC R2024 Regulations**.

---

### Project Structure

```text
├── codes/                   # Java source files (KECGui.java, Main.java, EvaluationEngine.java, etc.)
├── video/                   # Project demonstration video (Video-JDBC.mp4)
├── assets/                  # Branding assets (KEC official logo)
├── lib/                     # Dependency JARs (MySQL Connector, Apache PDFBox, jBCrypt)
├── migrations/              # Database SQL schema and migrations
├── Java_JDBC_PDF_Manual.pdf # Official Project Report & Lab Manual
└── README.md                # Documentation
```

---

### Project Documentation & Manual

The official project report and laboratory manual submitted for **24ITT32 - Programming in Java** by **Vikneshwaran N (25ITR173)** is included:
* 📄 **[Download / View Project Manual (Java_JDBC_PDF_Manual.pdf)](Java_JDBC_PDF_Manual.pdf)**

---

### Project Video Demonstration

A walkthrough video demonstrating the application features, continuous evaluation workflow, and JDBC database operations is available in the dedicated [`video/`](video/) folder:

* ⬇️ **[Direct Download Video (Video-JDBC.mp4)](https://github.com/VikneshwaranNv/Java_JDBC_project/raw/main/video/Video-JDBC.mp4)**
* 📁 **[Browse Video Folder](video/)**

---

### Key Capabilities

* **Curriculum & Course Management:** Supports Theory (40/60), Practical (60/40), and Embedded (50/50) assessment models with active enrollment protection.
* **Attendance Tracking & Condonation:** Enforces mandatory attendance rules ($\ge$75% Eligible, 65%–74.9% Medical Condonation Workflow, and $<$65% Detention / SA).
* **Authoritative R2024 Grading Engine:** Automatically calculates scaled internal marks, ESE thresholds, 10-point letter grades (`O` to `RA`, `SA`, `AB`), credit-weighted SGPA, and equivalent percentages.
* **Dual Interface:** Accessible via an institutional **Swing Desktop GUI** styled with official KEC branding and a 10-option **Interactive Terminal Console**.
* **Role-Based Access Control (RBAC):** Salted BCrypt password hashing with strict privilege tiers for Administrators, Faculty/Staff, and Students.
* **Official PDF Marksheets:** Generates institutional semester marksheet PDFs with college crest, course tables, and security signatures via Apache PDFBox.
* **Student Activity Points (SAP):** Tracks co-curricular and extra-curricular certifications, bonus rules, and faculty approval workflows.

---

### Tech Stack & Dependencies

* **Language:** Java 21 (OpenJDK 21)
* **GUI / CLI:** Java Swing & AWT / Terminal Console
* **Database:** MySQL 8.0 via JDBC (`jdbc:mysql://127.0.0.1:3306/kec_academic`)
* **Libraries:**
  * `mysql-connector-j-26.7.0.jar` — MySQL JDBC Driver
  * `pdfbox-app-2.0.31.jar` — Apache PDFBox PDF Generation
  * `jbcrypt-0.4.jar` — BCrypt Password Security

---

### Quick Start

1. **Compile Application:**
   ```bash
   javac -cp "lib/*" -d build codes/*.java
   ```

2. **Launch Swing Desktop GUI:**
   ```bash
   java -cp "build;lib/*" KECGui
   ```

3. **Launch Interactive Terminal Console:**
   ```bash
   java -cp "build;lib/*" Main
   ```

---

### Demo Accounts

* **Administrator:** `test_admin_sys` / `AdminSecret2026!`
* **Faculty / Staff:** `test_staff_kavitha` / `StaffSecret2026!`
* **Student:** `test_stud_arun` / `ValidStudentPass123!` (Roll: `24CSE001`)
