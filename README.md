# Campus Safety Monitor

Campus Safety Monitor is a full-stack operations console for campus transit and acoustic safety monitoring.

- **Frontend:** React dashboard served by Nginx
- **Backend:** Java 25, Spring Boot REST API, Spring Security
- **Data:** MySQL for accounts and speed violations, MongoDB for acoustic readings, Redis for short-lived transit and noise episode state

## Run the complete application

Install Docker Desktop, then start the stack from PowerShell with a strong initial administrator password (at least 12 characters):

```powershell
$env:CAMPUS_ADMIN_PASSWORD = "use-a-strong-private-password"
docker compose up --build
```

Open [http://localhost:5173](http://localhost:5173) and sign in with username `admin` and the password you configured. The username can be changed with `CAMPUS_ADMIN_USERNAME`. The initial administrator is created only when the user database is empty; passwords are stored as BCrypt hashes. **Do not use an example password in a shared or production environment.**

The API is also available at `http://localhost:8080`. The database ports are exposed locally for development. To stop the services, run `docker compose down`; persistent database volumes remain. To remove all persisted application data as well, explicitly run `docker compose down -v`.

## Dashboard features

- Safety overview with speed-violation and acoustic-reading totals, recent alerts, and a sound activity chart
- Transit checkpoint entry/exit forms with calculated vehicle speed and threshold violations
- Acoustic reading submission with sustained-noise event detection
- Live activity lists and CSV exports of up to 500 latest speed or acoustic records
- Administrator account management with `ADMIN` and `OPERATOR` roles
- Database-persisted monitoring thresholds editable in the admin settings
- Session-based authentication, CSRF protection, BCrypt password storage, and immediate account-disable enforcement

Operators can submit readings, use the transit forms, and review reports. Administrators can do all of that and manage accounts and safety settings. Public self-registration is not enabled.

## Local development

Start the databases:

```powershell
docker compose up -d mysql mongodb redis
```

Set the bootstrap admin password in the terminal where the backend will run, then start the API (requires Maven 3.9+ and Java 25):

```powershell
$env:CAMPUS_ADMIN_PASSWORD = "use-a-strong-private-password"
mvn spring-boot:run
```

In another terminal, start the React development server (requires Node.js 22+):

```powershell
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). Vite proxies `/api` calls to the Spring Boot server.

For backend tests, run `mvn test`. To build the frontend independently, run `npm run build` from `frontend`.

## API

All API routes require an authenticated session except the CSRF token and login endpoints.

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/auth/csrf` | Public | Start a CSRF-protected session |
| `POST` | `/api/auth/login` | Public | Sign in |
| `GET` | `/api/auth/me` | Signed in | Get the current account |
| `POST` | `/api/auth/logout` | Signed in | Sign out |
| `POST` | `/api/transit/entry` | Signed in | Record a vehicle at the first checkpoint |
| `POST` | `/api/transit/{vehicleNumber}/exit` | Signed in | Record the next checkpoint and calculate speed |
| `POST` | `/api/acoustics/readings` | Signed in | Submit and store an acoustic reading |
| `GET` | `/api/reports/summary` | Signed in | Read event totals |
| `GET` | `/api/reports/speed-violations` | Signed in | Read the latest 500 speed violations |
| `GET` | `/api/reports/acoustic-readings` | Signed in | Read the latest 500 acoustic readings |
| `GET`, `POST` | `/api/admin/users` | Administrator | List or create accounts |
| `PUT`, `DELETE` | `/api/admin/users/{id}` | Administrator | Update or delete an account |
| `GET`, `PUT` | `/api/admin/settings` | Administrator | Read or update safety thresholds |
| `GET` | `/actuator/health` | Public | Container health status |

For API clients, first request `/api/auth/csrf`, then send the returned token in the `X-XSRF-TOKEN` header for `POST`, `PUT`, and `DELETE` requests. Keep the session cookie and CSRF token together.

## Configuration

The safety settings initially use these defaults and can be changed from the administrator dashboard:

| Setting | Default |
| --- | ---: |
| Speed limit | 30 km/h |
| Checkpoint distance | 300 m |
| Transit entry TTL | 10 minutes |
| Noise threshold | 85 dB |
| Sustained noise duration | 3 seconds |
| Maximum sample gap | 2 seconds |

Connection settings can be configured with `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MONGODB_URI`, `REDIS_HOST`, and `REDIS_PORT`. In Docker Compose, service hostnames are configured automatically. `CAMPUS_FRONTEND_ORIGIN` controls the allowed browser origin when accessing the API directly. Enable `SESSION_COOKIE_SECURE=true` when serving the application over HTTPS.

The Compose database credentials and exposed database ports are intended for local development. Change them and restrict database network access before deploying to a shared environment.
