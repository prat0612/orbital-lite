# ORBITAL LITE – FULL STACK IMPLEMENTATION SPEC

You are a senior full-stack engineer.

Your task is to build a complete full-stack application named **Orbital Lite – Employee Directory** from scratch inside the current empty workspace.

---

# 🎯 OBJECTIVE

Build a production-ready full-stack application with:

Backend:

* Java + Spring Boot
* REST APIs
* Layered architecture

Frontend:

* React + Material UI
* Clean UI for CRUD operations

Database:

* H2 (default for development)
* PostgreSQL-ready configuration for future migration

---

# 📁 PROJECT STRUCTURE (MANDATORY)

Create this structure:

Orbital/
orbital-backend/
orbital-frontend/

---

# ⚙️ BACKEND REQUIREMENTS

Use:

* Spring Boot
* Spring Data JPA

Java compatibility:

* Use Java 17 compatibility

---

## Entity: Employee

Fields:

* id (Long, primary key, auto-generated)
* name (String, not null)
* email (String, unique, valid email)
* role (String)
* department (String)
* createdAt (timestamp)

---

## Layers:

* Controller
* Service
* Repository
* DTO (preferred)

---

## APIs:

1. Create Employee
   POST /api/employees

2. Get All Employees (pagination)
   GET /api/employees?page=0&size=10

3. Get Employee by ID
   GET /api/employees/{id}

4. Update Employee
   PUT /api/employees/{id}

5. Delete Employee
   DELETE /api/employees/{id}

6. Search Employees
   GET /api/employees/search?query=xyz

---

## Additional Requirements:

* Proper HTTP status codes
* Validation (name not null, email format)
* Global exception handling
* Logging
* CORS enabled

---

# 🗄️ DATABASE CONFIGURATION

## Profile-based configuration REQUIRED

### application.yml

* Common config

### application-h2.yml

* H2 database (default active)

### application-postgres.yml

* PostgreSQL configuration (ready but not active)

---

## H2 Config:

* In-memory DB
* Enable H2 console

---

## PostgreSQL Config:

* Placeholder config:

  * DB name: orbital_db
  * Username: postgres
  * Password: postgres

---

## RULE:

* Use JPA only
* Avoid native SQL queries

---

# 🎨 FRONTEND REQUIREMENTS

Use:

* React
* Material UI

---

## Pages:

### 1. Employee List Page

* Table view
* Pagination
* Search bar
* Edit/Delete buttons

### 2. Add/Edit Employee Form

* Form validation
* Submit to backend

---

## API Integration:

* Use Axios
* Base URL: http://localhost:8080/api

---

# 🔗 INTEGRATION

* Backend runs on port 8080
* Frontend runs on port 3000
* Enable CORS in backend

---

# 🧪 LOCAL RUN INSTRUCTIONS

Backend:

* mvn clean install
* mvn spring-boot:run

Frontend:

* npm install
* npm start

---

# 🐳 OPTIONAL (if time permits)

* Add Dockerfile for backend
* Add docker-compose (PostgreSQL ready)

---

# ☁️ DEPLOYMENT PLAN

Prepare for deployment on:

* Render OR AWS

Include:

* Environment variables
* Build steps

---

# 📄 OUTPUT REQUIREMENTS

You must:

1. Generate complete backend code
2. Generate complete frontend code
3. Provide setup instructions
4. Ensure app runs locally without errors

---

# ⚠️ IMPORTANT

* Follow clean architecture
* Keep UI simple
* Do not skip any layer
* Ensure no manual fixes required

---

# ✅ SUCCESS CRITERIA

* Full CRUD working
* Search working
* UI connected to backend
* App runs locally successfully
