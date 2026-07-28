# Contributing to rogic.io

Thank you for your interest in contributing to `rogic.io`! We welcome all contributions, from bug fixes and documentation to new features and UI improvements.

Please take a moment to review this guide to ensure a smooth contribution process.

---

## Technical Stack Overview

`rogic.io` is composed of several independent directory components:
* **`frontend/`**: client-side single-page application built on Vue 3, Vite, and HTML5 Canvas API.
* **`backend/`**: REST API built with Java 17 and Spring Boot (configured for GraalVM Native Image builds).
* **`infra/`**: Infrastructure-as-Code scripts utilizing Terraform (for AWS resource provisioning) and Ansible (configuration management).

---

## Contribution Workflow

### 1. Find or Create an Issue
Before making any code changes, ensure there is an open issue describing the bug or feature you wish to address. If not, please open one to discuss the proposed change.

### 2. Branch Naming Rules
Create a feature branch from the `main` branch. 
* Branch names should follow:
  * For issue-based work: `feat/#<issue_number>-<brief-description>` or `fix/#<issue_number>-<brief-description>`
  * For general enhancements: `feat/agent-<brief-description>` or `fix/agent-<brief-description>`

### 3. Local Development Setup

#### Frontend
1. Navigate to the `frontend/` directory.
2. Install dependencies: `npm install`
3. Launch development server: `npm run dev`

#### Backend & Database
1. Launch the local PostgreSQL instance using Docker Compose:
   ```bash
   docker compose up -d db
   ```
2. Navigate to the `backend/` directory.
3. Boot the Spring Boot application using your IDE or Gradle:
   ```bash
   ./gradlew bootRun
   ```

---

## Testing & Quality Constraints

We enforce strict test coverage, especially for core game engines (validators, hints, rotator modules).

### Mandatory TDD (Test-Driven Development)
When modifying or adding new core business logic (e.g. `Validator`, `HintCalculator`, `gridState` operations):
* **You MUST write the unit tests first.**
* Code submissions are only accepted if all test harnesses pass successfully.

### Running Verification Tests

#### Frontend Unit Tests
Execute the Vitest suite within the `frontend/` directory:
```bash
npm run test
```

#### Backend Unit Tests
Execute JUnit testing in the `backend/` directory:
```bash
./gradlew test
```

Please ensure that **both** suites pass locally before submitting your Pull Request.

---

## Commit Guidelines

We enforce the **Conventional Commits** specification for all commit logs to maintain clean logs:
* Prefix format: `<tag>: <description>` (all lower-case prefix).
* Representative Tags:
  * `feat`: New feature implementations
  * `fix`: Bug resolutions
  * `refactor`: Structural changes with no functional modifications
  * `docs`: Documentation edits (Markdown files, code comments)
  * `test`: Adding or updating test suites
  * `infra`: Terraform infrastructure or Ansible updates
  * `ci`: GitHub Actions pipeline updates

Example:
`feat: introduce global axios request interceptor for cognito session refresh`

---

## Pull Request Guidelines

1. Push your branch to the remote repository.
2. Open a **Draft Pull Request** targeting the `main` branch.
3. Link the PR to the corresponding issue by adding `Resolves #<issue_number>` or `Closes #<issue_number>` in the description body.
4. Once the build pipelines pass and a maintainer reviews the code, it will be approved, marked ready for review, and merged.
