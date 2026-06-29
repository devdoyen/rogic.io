# [INCIDENT_REPORTING]

## Trigger
Whenever a critical server error, production/stage deployment failure, database migration exception, or major system outage occurs and is resolved during developer operations.

## Instruction
You MUST generate a Postmortem (Incident Report) document inside the `docs/incidents/` directory.

## Filename Format
`YYYYMMDD_short_description.md` (e.g., `20260629_flyway_migration_failure.md`)

## Required Document Sections
1. **개요 (Summary):** Incident start/end time, total duration, scope of impact, and high-level cause.
2. **장애 타임라인 (Timeline):** Chronological sequence of events (detection, analysis, mitigation, validation, resolution) with timestamps.
3. **원인 분석 (Root Cause Analysis):** Detailed technical breakdown of why the failure occurred, including code snippets or SQL errors.
4. **해결 방안 (Resolution):** Description of the fix applied to resolve the incident, including diffs or query modifications.
5. **재발 방지 대책 (Preventative Actions):** Actionable steps to prevent similar failures in the future (architecture changes, validation procedures, testing strategy).
