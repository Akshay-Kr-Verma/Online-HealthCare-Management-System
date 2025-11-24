**Online Healthcare Management System**

--Project Description:
The aim of this project is to create an online healthcare management system for patients to book appointments, doctors to manage schedules, and administrators to oversee operations. Each user type has a dedicated dashboard for managing their respective activities.
This project is built using Java Swing (GUI), JDBC for database connectivity, and MySQL. It follows the MVC (Model-View-Controller) architecture.


--User Roles:
-Administrator: Manages user accounts (Doctors, Patients, Admins) and system settings.
-Doctor: Manages daily appointment schedules and updates patient records.
-Patient: Books appointments, views history, and manages personal profile.


--Technical Implementation:
-OOP Implementation: Uses Inheritance (base Dashboard classes), Polymorphism (User roles), and Interfaces (GenericDAO).
-Collections & Generics: Uses ArrayList and Generics to handle data fetching and display.
-Multithreading: Implements background threads for real-time clock features on dashboards without freezing the UI.
-Synchronization: Uses synchronized blocks for appointment booking to prevent data conflicts.
-Database Connectivity: Implements full CRUD operations using JDBC PreparedStatement and Transaction Management (commit/rollback).


--Database Setup
-Every time the app runs, a window pops up asking for user's MySQL password to establish database connection on localhost. After entering the correct password user can register themselves as per their role(patient or doctor) and continue to use the application.
-There's also hard coded credentials for admin, username: admin, password: admin123 .

--**How to Run**
-From a windows batch file named "run". 
Double-click the "run.bat" file located in the main project folder.
The application will compile and launch automatically.

Admin Login: admin / admin123

After succesful testing Patients and Doctors can register themselves and make, reschedule and cancel their appointments.
