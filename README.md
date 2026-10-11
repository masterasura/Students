# Students

A Java Swing student-management application built with Maven and MySQL.

## Requirements

- JDK 17 or newer
- Maven 3.8 or newer
- MySQL 8 or a compatible MySQL server

## Set up the database

1. Start your local MySQL server.
2. Run [`database/setup.sql`](database/setup.sql) using MySQL Workbench. From
   Command Prompt or Git Bash, you can instead run:

   ```text
   mysql -u root -p < database/setup.sql
   ```

   In PowerShell, use:

   ```powershell
   Get-Content .\database\setup.sql | mysql -u root -p
   ```

   The script creates the `student_management` database and the tables required
   by the application. It does not add example accounts or personal records.
3. Create a teacher account in the `student_management` database. Use your own
   local credentials; do not use a real or reused password:

   ```sql
   INSERT INTO teachers (username, name, password)
   VALUES ('teacher1', 'Teacher Name', 'choose-a-local-password');
   ```

   The current application stores passwords as plain text, so use only
   disposable credentials and test data.

## Configure and run

The application defaults to a MySQL server on `localhost:3306`, database
`student_management`, username `root`, and an empty password. Override these
settings for your own local MySQL account before launching:

```powershell
$env:STUDENTS_DB_URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=Asia/Manila&allowPublicKeyRetrieval=true"
$env:STUDENTS_DB_USER = "root"
$env:STUDENTS_DB_PASSWORD = "your-local-mysql-password"
mvn clean compile exec:java
```

Set `STUDENTS_DB_PASSWORD` to an empty string if your local MySQL account has no
password. The environment variables are read when the app starts. In an IDE,
set them in the run configuration before launching `com.mycompany.students.Main`.

## Share and collaborate on GitHub

1. Create an empty GitHub repository. Choose **Private** if the project or its
   history includes student or other personal information.
2. In this project folder, initialize Git and review what will be uploaded:

   ```powershell
   git init
   git add .
   git status
   ```

   Confirm the staged files contain source code, `pom.xml`, this README, and the
   database setup script—not generated output, local image folders, or personal
   data. Then make the first commit and connect the empty GitHub repository:

   ```powershell
   git commit -m "Prepare student management project"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
   git push -u origin main
   ```

3. Groupmates can clone the repository, set up their own database and
   environment variables, and run the app:

   ```powershell
   git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
   cd YOUR-REPOSITORY
   ```

4. For shared changes, each groupmate should create a feature branch, push it,
   and open a pull request rather than committing directly to `main`:

   ```powershell
   git switch -c feature/short-description
   # Make and test changes
   git add .
   git commit -m "Describe the change"
   git push -u origin feature/short-description
   ```

   Replace `feature/short-description` with a unique branch name. Review and
   merge the pull request on GitHub when the group is ready.

## Notes

- Maven downloads the MySQL Connector/J dependency declared in `pom.xml`.
- Build output (`target/` and `bin/`), compiled files, and local image folders
  are excluded from Git. The current Java source does not load files from those
  image folders.
- Do not commit database credentials, real student data, or files containing
  personal information.
