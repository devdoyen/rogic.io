# [DOCUMENTATION & FORMATTING GUIDELINES]

## 1. Markdown Formatting for Technical Context & Troubleshooting
- 마크다운 문서(예: `README.md`, `docs/` 내의 가이드 문서)에서 상세 기술 내역이나 트러블슈팅 사례를 기술할 때, 볼드 태그 소제목(예: `**배경**`, `**해결 방안**`, `**개발자 회고**`) 뒤에 콜론(`:`)을 붙이고 한 줄로 내용을 이어서 나열하지 않습니다.
- 마크다운의 파서에 따른 줄바꿈 병합 현상을 방지하고 시각적인 가독성을 보장하기 위해, 각 소제목 뒤에는 반드시 **`<br>` 태그**를 붙여 개행을 명시한 뒤 아랫줄에 들여쓰기를 적용하여 본문 내용을 기술해야 합니다.

*예시*:
```markdown
* **배경**<br>
  프로젝트의 메모리 제약 조건을 해결하기 위해...
* **해결 방안**<br>
  - **Agentless Pull 도입**<br>
    호스트 리소스를 차지하는 수집 데몬(Alloy)을 배제하고...
```

## 2. Relative Path Specification for File Links
- 프로젝트 내부 문서나 소스 코드 내에서 리포지토리의 다른 파일들을 마크다운 링크로 참조할 때, 로컬 개발 환경에 의존적인 절대 경로(예: `file:///c:/Users/...` 또는 `file:///` 스키마)를 절대 사용하지 않습니다.
- 깃허브(GitHub) 등 원격 플랫폼 상에서 웹뷰 상호 링크 호환과 다른 개발자 환경으로의 이식성을 완전 보장하기 위해 반드시 **리포지토리 루트 기준의 상대 경로(Relative Path, 예: `./docs/incidents/` 또는 `../.agents/rules/`)**로 참조 링크를 지정해야 합니다.

## 3. Data Representation for Numerical & Performance Comparison
- 비용(Billing), 시스템 사양, 벤치마크 지표, 처리량(Throughput) 등 기존 아키텍처 대비 개선 사항을 대조하여 설득력을 제시해야 하는 수치형 데이터는 괄호 및 문장을 이용한 일반 나열식 포맷을 지양합니다.
- 독자가 최적화 전후의 리소스와 비용 증감을 명확하고 직관적으로 인지할 수 있도록 마크다운 표(Table) 형식의 비교 테이블을 작성하여 구조화해야 합니다.
