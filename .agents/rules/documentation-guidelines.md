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
- 비교 테이블 내 기존 구성의 비용이나 수치를 측정하지 않은 경우, 의미가 불명확한 `-` 기호 대신 반드시 `N/A` 또는 `미측정`과 같은 명시적인 표기를 사용하고, 필요 시 각주(footnote)로 이유를 보충 기술합니다.

## 4. Heading Concision & Table of Contents Omission
- 마크다운 문서 내의 모든 대제목/소제목(Heading)을 설계할 때 다중 명사를 엔드 기호(`&`)로 길게 열거하여 가독성을 저해하는 장황한 형태를 지양합니다. 핵심을 관통하는 명확하고 간결한 단일 명사나 약어로 대표화하거나, 분할이 필요한 경우 계층적 하위 캡슐화(Subheading)를 수행하여 직관성을 극대화합니다.
- GitHub 마크다운 렌더러가 문서 제목들을 기반으로 아웃라인 목차(TOC)를 자동 파싱하여 제공하므로, `README.md` 등 주요 문서 상단에 불필요하고 유지보수가 번거로운 수동 링크식 목차 구문을 명시하지 않고 완전히 제거하여 관리합니다.

## 5. Heading Depth Limit & Flat Bold List
- 마크다운 헤딩 계층은 **최대 H4(`####`)까지만** 허용합니다. H5(`#####`) 이상의 깊이는 앵커 링크 없이는 탐색 자체가 불가능하여 독자 경험을 심각하게 저해합니다.
- H5 이상이 필요한 세부 항목은 반드시 볼드 리스트(`* **항목명**<br>`) 형식으로 평탄화하여 본문 내에 인라인으로 배치합니다. 이 방식은 TOC 추적이 필요 없는 세부 명세에 적합하며, 구조적 가독성을 유지합니다.

*예시*:
```markdown
#### 1.4.2.2. IAM Least Privilege Design
* **OIDC Keyless Authentication**<br>
  하드코딩된 AWS API Access Key 사용을 지양하고...
* **Service-Level Least Privilege Policy**<br>
  테라폼 및 Ansible 배포 범위에 정확히 부합하는...
```

## 6. Content Deduplication & Cross-Reference
- 동일한 기술 내용(예: GraalVM 메모리 최적화, Agentless Pull 구조, ALB 제거 근거)이 문서 내 여러 섹션에 분산 배치되는 중복 서술을 엄격히 금지합니다.
- 핵심 설명은 가장 적합한 섹션에 **단 한 곳**에만 서술하고, 나머지 위치에서는 반드시 마크다운 앵커 링크(예: `[1.3.1. Build Resource Constraints](#131-build-resource-constraints)`)를 통해 해당 섹션으로 참조 유도합니다.

## 7. Heading Language Consistency
- 하나의 문서 내에서 헤딩(Heading) 언어는 영어 또는 한국어 중 하나로 일관되게 통일합니다.
- 기술 문서(예: `README.md`)에서는 영어 헤딩을 기본으로 채택하며, 한국어 본문과 혼용하는 경우 헤딩만큼은 영어로 단일화합니다. 한국어 헤딩 혼입은 탐색 일관성을 무너뜨리고 앵커 링크 참조 오류를 유발할 수 있습니다.

*잘못된 예*:
```markdown
#### 1.4.2.1. 보안 그룹 (Security Group) 설정 및 허용 규칙   ← 한국어 혼입
#### 1.4.2.2. IAM Least Privilege Design                      ← 영어
```

*올바른 예*:
```markdown
#### 1.4.2.1. Security Group Configuration
#### 1.4.2.2. IAM Least Privilege Design
```
