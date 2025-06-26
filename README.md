# Virtual ATM system

Introduction:

The Virtual ATM System is a software-based simulation of an Automated Teller Machine (ATM) that allows users to perform essential banking operations such as balance inquiries, cash withdrawals, deposits, and fund transfers. This project is developed in Java using Object-Oriented Programming (OOP) principles and file handling for persistent data storage. The system ensures user authentication via an account number and PIN, providing a secure and user-friendly banking experience.

Objectives:

The main objectives of this project are:
•	✔ To develop a secure and efficient ATM system.
•	✔ To provide essential banking functionalities such as withdrawal, deposit, balance inquiry, and fund transfers.
•	✔ To implement user authentication for secure transactions.
•	✔ To store account details persistently using file handling.
•	✔ To ensure data integrity and prevent unauthorized access.


How to Run:

 - Enter your postgres password in the DatabaseConnection.java file and make a database in the postgres server name atm_db.
 - Compile the project using (javac -cp ".;openpdf-1.3.30.jar;flatlaf-3.4.jar;okhttp-4.9.3.jar;gson-2.13.1.jar;okio-2.8.0.jar;kotlin-stdlib-1.8.0.jar;postgresql-42.6.0.jar" (Get-ChildItem src\*.java))
 - Use cmd (java -cp ".;src;okhttp-4.9.3.jar;okio-2.8.0.jar;kotlin-stdlib-1.8.0.jar;gson-2.13.1.jar;flatlaf-3.4.jar;openpdf-1.3.30.jar;postgresql-42.6.0.jar" ATMGUI) to launch the application
