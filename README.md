This project is a simple CRUD (Create, Read, Update, Delete) web application for managing user data, built using Spring Boot as the backend framework, Vaadin for the frontend, and JasperReports for reporting. The project includes a reporting feature to generate a report based on the stored user data.

## Features
- CRUD Operations: Create, Read, Update, and Delete users
- Reporting: Generate reports of user data using JasperReports
- Database: H2 or other RDBMS options for data storage

## Table of Contents
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Usage](#usage)
- [Generating Reports](#generating-reports)

## Technologies Used
- Spring Boot – Backend framework
- Vaadin – Frontend framework for building UI
- JasperReports – Reporting engine for PDF reports
- H2 Database – In-memory database (optional switch to MySQL or PostgreSQL)
- Java 17 – Language version
- Maven – Dependency management

## Prerequisites
Make sure you have the following installed on your machine:
- Java 17+
- Maven 3+
- Git

## Setup Instructions
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/tarmizi-ahmad/manulife.git
   cd manulife


2. **Build the Project:** Use Maven to download dependencies and package the application
   ```bash
   mvn clean install

3. **Run the Application:** Start the Spring Boot application
   ```bash
   mvn spring-boot:run

4. **Access the Application:** Once the application is running, open your browser and visit:
   ```bash
   http://localhost:8080/users

5. **Access H2 Console:** H2 console can be accessed at:
   ```bash
   http://localhost:8080/h2-console
   
   Use the following credentials:
   
   JDBC URL: jdbc:h2:mem:testdb
   Username: sa
   Password: (leave it empty)

## Usage
1. Add a User: Use the web interface to fill out the user form and click "Save".
2. View Users: The home page will display a list of all users.
3. Edit User: Click on the "Update" button next to a user to modify their details.
4. Delete User: Click on the "Delete" button to remove a user from the list.
5. Text Filter: Type keyword at filter text, then press enter.

## Generating Reports
1. Click on the "Generate Report" button on the interface to download a PDF report of user data.
2. The report will be generated using JasperReports and create the link to download to your browser.
3. Click link download report here

## Troubleshooting
1. **Port Conflict:** If port 8080 is in use, change the port in application.properties:
   ```bash
   server.port=8081

2. **Database Issues:** Ensure that the H2 console is properly configured in application.properties:
   ```bash
   spring.h2.console.enabled=true
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driverClassName=org.h2.Driver
