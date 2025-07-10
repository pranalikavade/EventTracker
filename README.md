🎪 EventManagerApp 

 Java Console Project
A Java-based console application for managing events. Users can register, log in, view available events, register for events, and check their registrations.
 Administrators have additional privileges to add new events. The project leverages Core Java, JDBC, PostgreSQL, and Maven, designed with a modular structure in a single file (EventManagerApp) for simplicity
______________________________________________________________________________________________________________________________________________________________________________________________________________________
✨ Features
______________
🧾 User Registration & Login – Secure credential handling

📅 List Events – Browse upcoming events in chronological order

📝 Event Registration – Enroll into events and receive reminders

📄 View My Registrations – Users can review their registered events

👮‍♂️ Admin Mode – Add new events with date, time, capacity

🔔 Scheduled Reminders – Background reminders using multithreading

📋 Console Menu – Interactive, text-based user interface
___________________________________________________________________

💻 Technologies Used
________________________
☕ Core Java – OOP principles & collections

🔌 JDBC – Database connectivity

🗄️ PostgreSQL – Relational database storage

📦 Maven – Build and dependency management

🧠 Console-Based UI – Scanner-driven menu interface

🧵 Multithreading – Reminder scheduling via thread pool

_____________________________________________
🗃️ PostgreSQL Schema
___________________________

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL  -- 'user' or 'admin'
);


CREATE TABLE events (
  id SERIAL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  location VARCHAR(200),
  event_time TIMESTAMP NOT NULL,
  capacity INT NOT NULL
);


CREATE TABLE registrations (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id),
  event_id INT REFERENCES events(id),
  registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

*STRUCTURE
_____________________________________________________________
EventManagement/
├── Main.java
├── DBConfig.java
├── dao/
│   ├── UserDAO.java
│   ├── EventDAO.java
│   └── RegistrationDAO.java
├── model/
│   ├── User.java
│   ├── Event.java
│   └── Registration.java
└── service/
    ├── AuthService.java
    └── NotificationService.java
________________________________________________________________________
⚙️ Structure & Components
_____________________________________
DB Config: Establishes PostgreSQL JDBC connection

Model Classes: User, Event, Registration

DAO Layer: Handles database operations for users, events, and registrations

Service Layer:
AuthService: Manages authentication & registration

NotificationService: Uses ExecutorService to schedule reminders

Main App: Interactive console logic with user/admin flows and robust role checks
___________________________________________________________________________________
Devloper Info
__________________________________________________
Name:Pranali kavade

Email:pranalikavade73@gmail.com

github:pranalikavade

