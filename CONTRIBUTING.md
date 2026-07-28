# Contributing to rogic.io

Thank you for your interest in contributing to `rogic.io`! We welcome all contributions.

## Local Development Setup

1. **Database**: 
   ```bash
   docker compose up -d db
   ```
2. **Backend**:
   ```bash
   cd backend && ./gradlew bootRun
   ```
3. **Frontend**:
   * Create `frontend/.env.local` by copying `.env.example`.
   ```bash
   cd frontend && npm install && npm run dev
   ```

## Development Guidelines

1. **Branch Naming**: Please use descriptive branch names (e.g., `feat/add-dark-mode`, `fix/login-bug`).
2. **Commit Messages**: We follow [Conventional Commits](https://www.conventionalcommits.org/). Please prefix your commits with `feat:`, `fix:`, `docs:`, `chore:`, etc.
3. **Testing**: Please ensure both frontend and backend test suites pass locally before submitting a PR.
   * Backend: `./gradlew test`
   * Frontend: `npm run test`

## Pull Request Process

1. Open a Pull Request targeting the `main` branch.
2. Link the PR to any related issues (e.g., `Resolves #123`).
3. Once CI/CD pipelines pass and a maintainer reviews the code, it will be merged!
