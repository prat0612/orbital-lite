# Orbital Lite - Employee Directory

Orbital Lite is a full-stack employee directory built with Spring Boot, Spring Data JPA, React, Material UI, Axios, H2 for local development, and PostgreSQL-ready configuration for deployment.

## Project Structure

```text
Orbital/
  orbital-backend/
  orbital-frontend/
  docker-compose.yml
  render.yaml
```

## Git Setup

Initialize and commit the project:

```bash
git init
git add .
git commit -m "Initial commit - Orbital Lite"
```

Connect the local repository to GitHub:

```bash
git remote add origin <your-github-repo-url>
git branch -M main
git push -u origin main
```

If GitHub asks for authentication, use a GitHub Personal Access Token with repository write access or sign in through VS Code's GitHub integration.

## Local Backend

```bash
cd orbital-backend
mvn clean install
mvn spring-boot:run
```

The backend runs at `http://localhost:8080`.

Default profile is `h2`. H2 console is available at `http://localhost:8080/h2-console`.

H2 connection values:

```text
JDBC URL: jdbc:h2:mem:orbital_db
Username: sa
Password:
```

PostgreSQL profile:

```powershell
cd orbital-backend
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/orbital_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
mvn spring-boot:run
```

## Local Frontend

```bash
cd orbital-frontend
npm install
npm start
```

The frontend runs at `http://localhost:3000`.

Local frontend environment:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

The local value is stored in `orbital-frontend/.env`, and `orbital-frontend/.env.example` is committed as a template.

## Environment Variables

Backend:

```text
SPRING_PROFILES_ACTIVE=h2
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/orbital_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

Use `SPRING_PROFILES_ACTIVE=h2` for the in-memory database. Use `SPRING_PROFILES_ACTIVE=postgres` with the datasource variables for PostgreSQL.

Frontend:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

For deployment, set `VITE_API_BASE_URL` to the deployed backend URL ending in `/api`.

## API

```text
POST   /api/employees
GET    /api/employees?page=0&size=10
GET    /api/employees/{id}
PUT    /api/employees/{id}
DELETE /api/employees/{id}
GET    /api/employees/search?query=xyz
```

## Docker

Start PostgreSQL and the backend with the PostgreSQL profile:

```bash
docker compose up --build
```

The React frontend is run locally with `npm start` and will connect to the containerized backend on port `8080`.

## Render Deployment

Backend Web Service:

```text
Root Directory: orbital-backend
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/*.jar
Environment: SPRING_PROFILES_ACTIVE=h2
```

Backend environment variables:

```text
SPRING_PROFILES_ACTIVE=h2
SPRING_DATASOURCE_URL=<optional for postgres>
SPRING_DATASOURCE_USERNAME=<optional for postgres>
SPRING_DATASOURCE_PASSWORD=<optional for postgres>
```

Frontend Static Site:

```text
Root Directory: orbital-frontend
Build Command: npm install && npm run build
Publish Directory: dist
```

Frontend environment variables:

```text
VITE_API_BASE_URL=https://<your-render-backend-service>.onrender.com/api
```

Step-by-step Render flow:

1. Push this repository to GitHub.
2. In Render, create a new Web Service from the GitHub repository.
3. Set root directory to `orbital-backend`.
4. Set build command to `mvn clean package -DskipTests`.
5. Set start command to `java -jar target/*.jar`.
6. Set `SPRING_PROFILES_ACTIVE=h2` for the simplest deployment.
7. Create a new Static Site from the same GitHub repository.
8. Set root directory to `orbital-frontend`.
9. Set build command to `npm install && npm run build`.
10. Set publish directory to `dist`.
11. Set `VITE_API_BASE_URL` to the deployed backend URL ending in `/api`.

For PostgreSQL on Render, create a Render PostgreSQL database and change backend environment variables to:

```text
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=<Render internal database URL converted to jdbc:postgresql://...>
SPRING_DATASOURCE_USERNAME=<database username>
SPRING_DATASOURCE_PASSWORD=<database password>
```

## Render Blueprint

This repository includes `render.yaml` for a starting Blueprint configuration. Update the frontend `VITE_API_BASE_URL` value after Render creates the final backend URL.
