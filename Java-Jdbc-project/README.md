# 💻 Java JDBC Project — Source Code & Assets

This folder contains the complete source code, libraries, database migrations, and assets for **KEC ACADEMIX — Student Academic Evaluation & Management System**.

---

### 📂 Folder Structure

```text
├── codes/                   # Java source files (KECGui.java, Main.java, EvaluationEngine.java, etc.)
├── lib/                     # JDBC and third-party libraries (MySQL Driver, PDFBox, jBCrypt)
├── assets/                  # Branding and UI assets (Official KEC logo)
├── migrations/              # Database SQL schemas and migrations
├── backup/                  # Historical backups and previous versions
├── scratch/                 # Diagnostic and verification test utilities
├── uploads/                 # Storage for student certificate uploads
├── .classpath               # Eclipse/VS Code Java project classpath
└── .project                 # Project configuration
```

---

### 🚀 Compilation & Execution

Open a terminal in this directory (`Java-Jdbc-project`):

1. **Compile the Application:**
   ```bash
   javac -cp "lib/*" -d build codes/*.java
   ```

2. **Run Desktop Swing GUI:**
   ```bash
   java -cp "build;lib/*" KECGui
   ```

3. **Run Terminal Console (CLI):**
   ```bash
   java -cp "build;lib/*" Main
   ```

---

### 🔑 Default Demo Accounts

* **Administrator:** `test_admin_sys` / `AdminSecret2026!`
* **Faculty / Staff:** `test_staff_kavitha` / `StaffSecret2026!`
* **Student:** `test_stud_arun` / `ValidStudentPass123!` (Roll: `24CSE001`)
