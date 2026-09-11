📋 Taskly — Full-Stack Task Management & Kanban Application

Taskly is a full-stack task management application designed to help users organize, track, and manage tasks through a Kanban-style workflow.

The application combines a Spring Boot backend with a React frontend and includes authentication, role-based authorization, task management, drag-and-drop workflows, and an administrative dashboard.

✨ Features

🔐 JWT Authentication — Secure user authentication using JWT and Spring Security.

👥 Role-Based Authorization — Separate access and functionality for USER and ADMIN roles.

📋 Task Management — Create, view, update, and delete tasks.

🔄 Kanban Workflow — Manage tasks across To Do, In Progress, and Done statuses.

🖱️ Drag-and-Drop — Move tasks between Kanban columns using React DnD.

🎯 User-Specific Tasks — Users can access and manage their own tasks.

📄 Pagination & Sorting — Paginated and sortable task and administrative listings.

🛡️ Validation & Exception Handling — Request validation and centralized handling of application errors.

🖥️ Admin Dashboard — Manage users and tasks through dedicated administrative workflows.

📱 Responsive UI — React-based interface designed for different screen sizes.

🔔 User Feedback — Loading states, error handling, toast notifications, and confirmation dialogs.

🛠️ Tech Stack

Backend

Java

Spring Boot

Spring Security

JWT

Spring Data JPA

Hibernate

REST APIs

Maven

Frontend

React.js

React Router

React Hook Form

React DnD

Axios

HTML5

CSS3

JavaScript

Database & Testing

MySQL

JUnit

Mockito

Version Control

Git

GitHub

🏗️ Application Structure

The project follows a layered backend structure with a separated React frontend.

Backend

Controllers

Services

Repositories

DTOs

Entities

Mappers

Exception handling

Security

Frontend

Components

Pages

Routing

API/service layer

Forms

Authentication and protected routes

🔒 Security

Taskly uses Spring Security and JWT to protect application workflows.

JWT-based authentication

Protected REST APIs

Role-based authorization

Protected frontend routes

Separate USER and ADMIN access

🧪 Testing

Core application functionality is tested using JUnit and Mockito, including areas such as:

Authentication

Task CRUD operations

Authorization

Validation

Exception handling

Task status updates

Administrative functionality

🚀 Getting Started

Prerequisites

Make sure the following are installed:

Java

Maven

Node.js and npm

MySQL

1. Clone the repository

git clone https://github.com/ektas-dev/kanban-board-task-management.git
cd kanban-board-task-management

2. Configure MySQL

Create a MySQL database for the application and update the backend database configuration with your local credentials.

Keep sensitive credentials out of source control.

3. Start the backend

Navigate to the backend directory and install/build the project using Maven.

mvn clean install

Then start the Spring Boot application using the project's configured Spring Boot run configuration.

4. Start the frontend

Navigate to the frontend directory:

npm install

Then run the frontend using the start script configured in package.json.

📌 Project Highlights

Taskly demonstrates practical implementation of:

Full-stack application development

REST API design

JWT authentication and Spring Security

Role-based access control

JPA/Hibernate data persistence

DTO-based API communication

Input validation and exception handling

Pagination and sorting

React routing and protected navigation

Drag-and-drop Kanban interaction

Unit testing with JUnit and Mockito

👩‍💻 Author

Ekta S

Java Full-Stack Developer

GitHub
