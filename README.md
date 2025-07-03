# Prototype Payroll System Version 2

A full-featured payroll management desktop application built in Java using NetBeans and Apache Ant. This system supports HR, payroll, employee management, leave tracking, payslip generation, and authentication.

## 📌 Features

* Employee record management
* Payroll processing and salary computation
* Leave application and tracking
* Payslip generation and printing
* Authentication and user roles (Administrator, HR, Team Leader, Payroll Manager, and Employee)
* GUI interface for all modules
* Modular code structure with reusable utilities

## 🛠 Tech Stack

* **Language**: Java
* **Build Tool**: Apache Ant (`build.xml`)
* **IDE**: NetBeans (uses `nbproject` folder)
* **UI**: Java Swing
* **Data Storage**: Local flat file or embedded database (e.g., serialization or embedded DB like H2)

## 📂 Project Structure

```
src/
├── accounting/
├── admin/
├── auth/
├── employee/
├── gui/
├── hr/
├── leave/
├── payroll/
├── payrollmanager/
├── payslip/
├── resources/
├── teamleaderview/
├── utils/
```

## 🚀 Getting Started

1. **Clone the repository:**
   `git clone https://github.com/jomax001/Prototype-Payroll-System-Version2`

2. **Open in NetBeans:**
   Use NetBeans to open the project folder. The IDE should recognize the project automatically via `nbproject`.

3. **Build the project:**
   Use "Clean and Build Project" or run `build.xml` via Ant.

4. **Run the application:**
   Execute the main GUI class or use NetBeans' Run option.

## 📝 Notes

* Ensure Java is installed and properly configured.
* Additional setup might be needed for database paths or configuration files.

## 👤 Author

**jomax001**
GitHub: [https://github.com/jomax001](https://github.com/jomax001)

## 📄 License

This project is currently in prototype stage. License to be determined.
