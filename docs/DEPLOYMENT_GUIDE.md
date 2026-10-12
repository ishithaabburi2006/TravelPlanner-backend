# Deployment Guide

## Prerequisites

- Java 21 and Maven 3.9+
- Node.js 20.19+ or 22.12+
- MySQL 8 with an InnoDB database

## MySQL setup

Use a dedicated application account. A DBA can create it with only data access; schema changes are applied separately:

```sql
CREATE DATABASE travel_planner CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'travel_planner_app'@'localhost' IDENTIFIED BY 'replace-with-a-long-local-secret';
GRANT SELECT, INSERT, UPDATE, DELETE ON travel_planner.* TO 'travel_planner_app'@'localhost';
```

Select the `travel_planner` database and apply `database/schema.sql` using a DBA account, then configure the application to use the restricted account. For a database created by an older release, make a verified backup first, inspect the existing schema, then apply `database/migrations/manual_booking_travelers.sql` exactly once if `booking.travelers` is absent. Do not run the clean-install schema over an existing database. No migration runner is configured, so migrations must be reviewed and applied by an operator.

## Environment configuration

Copy `backend/.env.example` to `backend/.env`. Set a real local DB username/password and generate `JWT_SECRET` with `openssl rand -base64 32`. Optionally set `ADMIN_EMAIL`, `ADMIN_NAME`, and a unique `ADMIN_PASSWORD` to create the first admin account if that email does not already exist. The seeder does not change the password or role of an existing account.

Copy `frontend/.env.example` to `frontend/.env` only when the API is not at its default address. Set `CORS_ALLOWED_ORIGINS` to the exact frontend origin. Never commit or package `.env` files; `.env.example` files are templates only. Configure production secrets through the deployment platform's secret manager.

## Run locally

From the repository root, open separate terminals:

```sh
./start-backend.sh
./start-frontend.sh
```

Open `http://localhost:5173`. The local profile is not a relaxed database mode: Hibernate uses `validate` and does not create or change tables. Database setup is still required before startup.

## Deploy to Railway

The repository contains Dockerfiles for the API and Vite client. Create a Railway project with three services: MySQL, Backend, and Frontend. Connect both application services to the same GitHub repository. Railway's monorepo setup uses each service's root directory (`/backend` and `/frontend`); Railway detects the Dockerfile in each root.

1. Add a MySQL database service. Keep it private and retain its generated volume. Before deploying the API, select the database named by Railway's `MYSQLDATABASE` variable and apply `database/schema.sql` to that empty database. Use the Railway database's connection tool or a temporary TCP proxy; do not apply this clean-install schema to an existing database.
2. Set Backend service variables in Railway. Use Railway reference variables for the MySQL service (assuming it is named `MySQL`):

   ```text
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC&characterEncoding=UTF-8
   DB_USERNAME=<dedicated app account created in the Railway database>
   DB_PASSWORD=<dedicated app account password; do not use the database admin password>
   JWT_SECRET=<generate locally with openssl rand -base64 32 and paste directly into Railway>
   JWT_ISSUER=travel-planner-api
   JWT_EXPIRATION=86400000
   JPA_DDL_AUTO=validate
   SERVER_PORT=8080
   CORS_ALLOWED_ORIGINS=<frontend https origin after generating its Railway domain>
   ADMIN_EMAIL=<your administrator email>
   ADMIN_NAME=Travel Planner Admin
   ADMIN_PASSWORD=<unique password of at least 12 characters>
   ```

   Enter real secret values directly in Railway's Variables editor. Do not commit them, paste them into chat, or place them in the repository's `.env.example` files. The app creates the configured administrator only if that email does not already exist; it does not reset an existing account.
3. Deploy Backend and generate a Railway public domain. The service listens on port 8080. Set Frontend's `API_BASE_URL` to `https://<backend-domain>/api`.
4. Deploy Frontend and generate its Railway public domain. It serves the SPA on port 8080 and uses `API_BASE_URL` at container startup, so setting it does not require rebuilding the static Vite bundle.
5. Set Backend's `CORS_ALLOWED_ORIGINS` to the exact Frontend origin, such as `https://<frontend-domain>` (no path or trailing slash), and redeploy Backend.
6. Confirm the frontend loads, public destination APIs respond, registration/login work, and authenticated itinerary/booking flows persist in the Railway MySQL database. Do not use production customer data for this smoke test.

Railway documents the monorepo root-directory and Dockerfile behavior in its [monorepo guide](https://docs.railway.com/deployments/monorepo) and [Dockerfile guide](https://docs.railway.com/builds/dockerfiles). The MySQL service exposes private connection variables, which should be linked using [Railway reference variables](https://docs.railway.com/variables/reference). Generated public domains are managed under service networking settings.

Database initialization is intentionally an operator action, not an application startup side effect. Back up and migrate existing databases separately. Use Railway's database backup features and periodically test restoration; backup retention and recovery objectives must be configured for the selected plan.

## Build and run locally

```sh
cd backend
mvn clean test
mvn clean package
java -jar target/travel-planner-api-1.0.0.jar

cd ../frontend
npm ci
npm run build
npm run preview
```

For production, use HTTPS, exact CORS origins, managed secrets, and a MySQL account without DDL permissions. Hibernate validates the schema at startup and does not create or alter tables. This project does not implement automated schema migrations, API health monitoring, rate limiting, or an end-to-end release gate.

## Backups and restore

Use encrypted, access-controlled backups. A logical backup and restore can be performed with:

```sh
mysqldump --single-transaction --routines --triggers travel_planner > travel_planner.sql
mysql travel_planner < travel_planner.sql
```

Restore into a disposable instance and verify the application before relying on a backup. The repository does not define backup retention or recovery objectives.
