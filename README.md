# Campus Safety Monitor

A dashboard for recording vehicle speeds between campus checkpoints and tracking noise levels in campus zones. Operators can enter readings and review recent activity; administrators can also manage accounts and monitoring thresholds.

## Stack

- React and Vite, served by Nginx
- Java 25 and Spring Boot
- MySQL for accounts, settings, and speed violations
- MongoDB for acoustic readings
- Redis for in-progress transit and noise tracking

## Run with Docker

Install Docker Desktop, then run these commands in PowerShell from the project folder:

```powershell
$env:CAMPUS_ADMIN_PASSWORD = "choose-a-private-password-at-least-12-characters"
docker compose up --build
```

Open [http://localhost:5173](http://localhost:5173). Sign in with username `admin` and the password you set above. Set `CAMPUS_ADMIN_USERNAME` to choose a different username.

The initial administrator is created only when the user database is empty. Passwords are stored as BCrypt hashes. Keep the password private; do not use a sample password for a shared deployment.

The API is available at [http://localhost:8080](http://localhost:8080). To stop the containers, run:

```powershell
docker compose down
```

Database data is kept in Docker volumes. To remove that data too, run `docker compose down -v`.

## Run locally

Requirements: Java 25, Maven 3.9+, Node.js 22+, and Docker Desktop.

Start the databases:

```powershell
docker compose up -d mysql mongodb redis
```

In one terminal, start the backend:

```powershell
$env:CAMPUS_ADMIN_PASSWORD = "choose-a-private-password-at-least-12-characters"
mvn spring-boot:run
```

In another terminal, start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

Visit [http://localhost:5173](http://localhost:5173). Vite forwards `/api` requests to the backend.

Run backend tests from the project folder with `mvn test`. To build the frontend, run `npm run build` from `frontend`.

## What it does

- Records vehicle entry and exit times at checkpoints and calculates speed from the configured distance.
- Flags speed violations and detects sustained noise above the configured threshold.
- Shows recent activity, summary counts, and CSV exports of up to 500 recent readings.
- Provides `ADMIN` and `OPERATOR` accounts. Operators can submit readings and review reports; administrators can also manage users and settings.
- Uses session-based sign-in, CSRF protection, BCrypt password hashing, and account-disable enforcement.

Public self-registration is disabled.

## API overview

All routes require a signed-in session except the CSRF and login routes and the health check.

| Method | Route | Access |
| --- | --- | --- |
| `GET` | `/api/auth/csrf` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | Signed in |
| `POST` | `/api/auth/logout` | Signed in |
| `POST` | `/api/transit/entry` | Signed in |
| `POST` | `/api/transit/{vehicleNumber}/exit` | Signed in |
| `POST` | `/api/acoustics/readings` | Signed in |
| `GET` | `/api/reports/summary` | Signed in |
| `GET` | `/api/reports/speed-violations` | Signed in |
| `GET` | `/api/reports/acoustic-readings` | Signed in |
| `GET`, `POST` | `/api/admin/users` | Administrator |
| `PUT`, `DELETE` | `/api/admin/users/{id}` | Administrator |
| `GET`, `PUT` | `/api/admin/settings` | Administrator |
| `GET` | `/actuator/health` | Public |

API clients should request `/api/auth/csrf` first, then include the returned token in the `X-XSRF-TOKEN` header on `POST`, `PUT`, and `DELETE` requests. Keep the session cookie and token together.

## Default monitoring settings

| Setting | Default |
| --- | ---: |
| Speed limit | 30 km/h |
| Checkpoint distance | 300 m |
| Transit entry expiry | 10 minutes |
| Noise threshold | 85 dB |
| Sustained noise duration | 3 seconds |
| Maximum gap between samples | 2 seconds |

Administrators can change these settings in the dashboard.

## Configuration

The database connections can be set with `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MONGODB_URI`, `REDIS_HOST`, and `REDIS_PORT`. Docker Compose supplies the correct service hostnames for local use.

`CAMPUS_FRONTEND_ORIGIN` sets the allowed browser origin for direct API access. Set `SESSION_COOKIE_SECURE=true` when serving over HTTPS.

The database credentials and exposed database ports in `compose.yaml` are for local development. Change the credentials and restrict database access before using the application in a shared environment.
