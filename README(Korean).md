# Invaders-SDP-Pro9
Space Invaders Extension by Team Pro9 in SDP-24788
# Space Invaders (Team Pro9)

> **Team Pro9의 Space Invaders 게임 개발 프로젝트** > Java 기반의 고전 슈팅 게임을 리팩토링하고, 아이템 시스템, 상점, 보스전 등 새로운 기능을 추가하여 현대적으로 재해석했습니다.

---

## 1. 프로젝트 정보 (Project Info)

### 👥 팀원 소개
| 이름 | 역할 | 담당 파트 |
| :--- | :--- | :--- |
| **염승민** | Team Leader | 총괄, Player 로직 구현 |
| **윤승주** | Developer | 로그인, 상점 시스템, Enemy 로직 |
| **이유빈** | Developer | Boss 로직 구현 |
| **김태우** | Developer | Item 시스템 구현 |
| **지도윤** | Resource Manager | 외부 리소스(BGM, SFX) 서치 |

---

## 2. 프로젝트 구성 및 환경 (Configuration & Environment)

이 프로젝트는 다음 환경과 라이브러리를 기반으로 개발되었습니다.

* **언어 (Language)**: Java 21
* **빌드 도구 (Build Tool)**: Gradle 8.7 (Kotlin DSL)
* **IDE**: IntelliJ IDEA

### 🛠 주요 의존성 (Dependencies)
`build.gradle.kts`에 정의된 주요 라이브러리는 다음과 같습니다.

* **테스트 프레임워크**: JUnit 5 (v5.10.2)
    * `org.junit.jupiter:junit-jupiter-api`
    * `org.junit.jupiter:junit-jupiter-engine`
    * `org.junit.jupiter:junit-jupiter-params`
* **테스트 모킹 (Mocking)**: Mockito (v5.11.0 / Inline v5.2.0)
    * `org.mockito:mockito-core`
    * `org.mockito:mockito-junit-jupiter`
    * `org.mockito:mockito-inline` (Static 메소드 모킹 지원)
* **코드 커버리지**: JaCoCo (v0.8.10)

### 📅 Project Management
* **JIRA (Kanban Board)**: [Project Link](https://sdp-25-pro9.atlassian.net/jira/software/projects/TP/boards/2)
* **Communication**: KakaoTalk, Slack (회의록), Zoom (비정기 회의)
* **Regular Meeting**: 매주 화요일 오후 2:00

### 🌿 Branch Strategy
**Simplified Git Flow**를 사용합니다. 개발 인원이 적으므로 복잡한 구조 대신 효율적인 워크플로우를 지향합니다.

1. `main`: 배포 가능한 안정 버전 브랜치
2. `develop`: 개발 중인 코드가 모이는 통합 브랜치
3. `feature/*`: 각 개발자가 기능을 구현하는 개인 작업 브랜치
    * **작업 흐름**: `feature/개인작업` → (PR & Merge) → `develop` → `main`

### 📝 Code Style
**Google Java Style Guide**를 준수합니다.

* **들여쓰기**: Space 4칸
* **주석**: 모든 메소드에 param, return, throws 정보를 상세히 작성 (Javadoc)
* **명명 규칙**
    * Class: `PascalCase`
    * Variable, Method: `camelCase`
    * Variable: 명사형, 축약어 금지 (Full Name 사용)
    * Method: 동사형
    * Constant: `UPPER_SNAKE_CASE` (예: `PI`, `MAX_SIZE`)
    * Branch: `feature/a-b` (소문자)

### 🔏 Commit Convention
**"모든 커밋에는 한 가지 기능만 포함한다"**

1. **Subject (제목)**: `태그: 제목` (예: `Feat: Add lazer type boss logic`)
2. **Body (본문)**: 어떻게 변경했는지보다 **무엇을, 왜** 변경했는지 설명합니다.

### 🔄 PR Convention
* 하나의 Epic 아래에 있는 **한 개의 Task가 끝나면** PR을 요청합니다.

---

## 3. 시작하기 (Getting Started)

### 저장소 클론 (Clone)
터미널에서 아래 명령어를 입력하여 프로젝트를 다운로드합니다.

```bash
git clone [https://github.com/invaders-sdp-pro9/Invaders-SDP-Pro9.git](https://github.com/invaders-sdp-pro9/Invaders-SDP-Pro9.git)
cd Invaders-SDP-Pro9
