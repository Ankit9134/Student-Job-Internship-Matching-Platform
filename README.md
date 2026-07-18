# Student Job Matching Platform

A full-stack job matching platform that ranks job listings for students based on skills, GPA, work authorization, and work mode preferences. Recruiters can post listings and view top matching candidates.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 21, Tailwind CSS 4, Lucide Icons |
| Backend | Spring Boot 3.4.5, Java 17 |
| Database | MySQL |
| Auth | Spring Security + JWT (JJWT 0.11.5) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven (Backend), Angular CLI (Frontend) |

---

## Project Structure

```
Student Dashboard/
├── Backend/          # Spring Boot REST API
└── Frontend/         # Angular SPA
```

---

## Backend Architecture

### Package Structure

```
com.example.jobmatch/
├── config/
│   ├── CorsConfig.java              # CORS — allows http://localhost:4200
│   ├── JwtFilter.java               # JWT request filter (per-request auth)
│   ├── JwtUtil.java                 # Token generation & validation
│   ├── MatchingWeightsConfig.java   # Configurable score weights from properties
│   └── SecurityConfig.java         # Spring Security — public vs protected routes
│
├── controller/
│   ├── AuthController.java          # POST /api/auth/register, /api/auth/login
│   ├── StudentController.java       # CRUD profile, upload resume, get matches
│   ├── ListingController.java       # CRUD listings, applicant counts, top students
│   ├── ApplicationController.java   # Mark applied, update status, delete
│   └── SkillController.java         # GET /api/skills (skill tag picker)
│
├── service/
│   ├── StudentService.java          # Profile create/update, resume URL
│   ├── ListingService.java          # Listing CRUD, skill weights
│   ├── MatchingService.java         # Score computation engine (bulk-optimized)
│   ├── MatchQueryService.java       # Read match_results with filters & pagination
│   ├── ApplicationService.java      # Apply, status update, notes, delete
│   └── FileStorageService.java      # Resume file storage to disk
│
├── repository/                      # Spring Data JPA repositories
├── entity/                          # JPA entities (Student, Listing, Application, ...)
├── dto/                             # Request/Response DTOs
└── exception/                       # GlobalExceptionHandler, ResourceNotFoundException
```

### Key Design Decisions

**Match Score Computation**
Scores are pre-computed and cached in `match_results` table — not calculated on every read. Recomputation is triggered on profile save or listing create/update.

Score formula:
```
finalScore = skillScore × 0.60
           + gpaScore   × 0.20
           + authScore  × 0.20
```
Weights are configurable via `application.properties`.

**N+1 Prevention**
- `recomputeForListing` loads all student skills in one query, groups in-memory
- `recomputeForStudent` loads all listing skills in one query via `loadListingSkillsMap()`
- `ApplicationService.getForStudent` uses `JOIN FETCH` to load applications + listings in one query

**Indexes on `match_results`**
```sql
INDEX idx_mr_student_score (student_id, score DESC)
INDEX idx_mr_listing_score (listing_id, score DESC)
```

### API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/students` | Create student profile |
| GET | `/api/students/{id}` | Get student profile |
| PUT | `/api/students/{id}/profile` | Update profile + recompute matches |
| POST | `/api/students/{id}/resume` | Upload resume file |
| GET | `/api/students/{id}/resume` | View/download resume |
| GET | `/api/students/{id}/matches` | Paginated, filtered, ranked matches |
| POST | `/api/students/{id}/applications` | Mark listing as applied |
| GET | `/api/students/{id}/applications` | List all applications |
| PATCH | `/api/applications/{id}` | Update status + notes |
| DELETE | `/api/applications/{id}` | Remove application |
| GET | `/api/listings` | Browse all listings |
| POST | `/api/listings` | Create listing (recruiter) |
| PUT | `/api/listings/{id}` | Update listing |
| DELETE | `/api/listings/{id}` | Delete listing |
| GET | `/api/listings/{id}/matches` | Top matching students for listing |
| GET | `/api/listings/applicants/counts` | Bulk applicant counts (all listings) |
| GET | `/api/skills` | All available skills |

### Configuration (`application.properties`)

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3308/jobmatch
app.matching.weight-skill=0.60
app.matching.weight-gpa=0.20
app.matching.weight-auth=0.20
app.upload.resume-dir=./uploads/resumes
app.cors.allowed-origin=http://localhost:4200
app.jwt.expiration-ms=86400000   # 24 hours
```

---

## Frontend Architecture

### Project Structure

```
src/app/
├── components/
│   ├── auth/              # Login & Register forms
│   ├── profile/           # Student profile editor + resume upload
│   ├── matches/           # Ranked job matches with filters & pagination
│   ├── match-card/        # Individual match card with score ring & breakdown
│   ├── applications/      # Application tracker with status + notes
│   └── admin/             # Recruiter dashboard — listings CRUD + top students
│
├── services/
│   ├── auth.service.ts        # Login/register, JWT storage
│   ├── student.service.ts     # Profile CRUD, resume upload/view URL
│   ├── match.service.ts       # Fetch ranked matches
│   ├── application.service.ts # Apply, update status, delete
│   ├── listing.service.ts     # Listings CRUD, applicant counts
│   ├── skill.service.ts       # Fetch skill list
│   └── auth.interceptor.ts    # Attaches JWT Bearer token to every request
│
├── guards/
│   └── auth.guard.ts      # studentGuard, recruiterGuard — role-based route protection
│
├── models/
│   ├── student.model.ts   # StudentProfileRequest/Response, Skill, enums
│   ├── match.model.ts     # MatchCard, MatchBreakdown, ApplicationRecord, PagedResponse
│   └── listing.model.ts   # Listing model
│
├── pipes/
│   └── match-score.pipe.ts  # Formats score as percent or label (Strong/Good/Low)
│
└── app.routes.ts          # Route definitions with guards
```

### Routing

| Path | Component | Guard |
|---|---|---|
| `/login` | AuthComponent | — |
| `/profile` | ProfileComponent | studentGuard |
| `/matches` | MatchesComponent | studentGuard |
| `/applications` | ApplicationsComponent | studentGuard |
| `/admin` | AdminComponent | recruiterGuard |

### State Management
No external state library — components use Angular services with RxJS Observables. Each component owns its local loading/error state.

### Key Patterns

- **Standalone components** — no NgModules, each component declares its own imports
- **Reactive Forms** — profile and listing forms use `FormBuilder` with validators
- **Auth Interceptor** — `auth.interceptor.ts` automatically attaches `Authorization: Bearer <token>` to all API calls
- **Loading states** — `loading = true` initialized so spinner shows immediately on navigation
- **Toast notifications** — fixed top-right toast with 4s auto-dismiss for save success/error

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+ / npm
- MySQL running on port `3308` with database `jobmatch`

### Backend

```bash
cd Backend
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### Frontend

```bash
cd Frontend
npm install
ng serve
# App available at http://localhost:4200
```

### Database Setup
Create the database before starting the backend:
```sql
CREATE DATABASE jobmatch;
```
Hibernate will auto-create tables on first run (`ddl-auto=update`).

---

## Features

### Student
- Register / Login with JWT auth
- Build profile — GPA, work auth, skills, preferred work mode & locations
- Upload resume (PDF/DOC/DOCX, max 5MB) — view directly in browser
- View ranked job matches with explainable score breakdown
- Filter matches by role, location, work mode, sponsorship
- Mark listings as applied — track status (Applied → Interviewing → Offer / Rejected)
- Add notes to each application

### Recruiter
- Post, edit, delete job listings with skill weights (Required / Nice-to-have)
- View all listings with live applicant counts
- See top matching students per listing with full profile modal
- Match breakdown per student — skill, GPA, work auth scores
