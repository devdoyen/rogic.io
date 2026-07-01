# [GIT & COMMIT CONVENTIONS]

## 1. Commit Message Convention
- 커밋 메시지는 Conventional Commits 사상을 기반으로 명확하고 일관된 영문 소문자 prefix(태그)를 사용하여 기술합니다.
- 대표적인 Prefix 목록:
  - `feat`: 신규 기능 구현
  - `fix`: 버그 해결 및 수정
  - `refactor`: 코드 구조 개선 (기능 변화 없음)
  - `docs`: README.md, progress_state.md 등 마크다운 문서 및 주석 변경
  - `infra`: Terraform 구성, Grafana 대시보드 등 인프라 형상 및 모니터링 변경
  - `ci`: GitHub Actions 워크플로우 등 CI/CD 파이프라인 설정 변경
  - `test`: 단위 테스트 및 검증 테스트 케이스 추가/수정
  - `chore`: 빌드 스크립트 수정, 패키지 설정 변경 등 단순 보조 업무
- 형식 예시:
  - `infra: convert SLA dashboard metrics to dynamic global range picker`
  - `ci: add terraform path filter to prevent deployment pipeline skips`

## 2. Commit & Push Lifecycle Governance
- **자동 커밋 정책**:
  - 각 기능 개발 또는 인프라 작업 완료 후, 변경된 파일들을 스테이징하고 정의된 컨벤션에 의거해 로컬 커밋(`git commit`)까지 자동으로 완료하여 변경 내역을 안전하게 보존합니다.
- **원격 푸시(Push) 통제 규칙**:
  - 기본적으로 에이전트는 원격 저장소로 직접 `git push`를 수행하지 않고 커밋까지만 완료하여 개발자에게 제어권을 위임합니다.
  - 단, **개발자가 명시적으로 push를 요청한 경우(예: "push 해줘")**, 또는 **커밋 해시 꼬임 복구 등 특이사항으로 인해 명확하게 승인/합의를 거친 경우**에 한하여 에이전트가 예외적으로 `git push`를 수행할 수 있습니다.
