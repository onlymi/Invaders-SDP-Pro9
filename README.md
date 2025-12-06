# Space Invaders (Project pro9)

> **Team Pro9's Space Invaders Development Project**
>
> A modern reinterpretation of the classic Java-based shooter game. We have refactored the original code and added new features such as an item system, a store, and boss battles.

---

## 1. Project Info

### 👥 Team Members
| Name | Role | Responsibilities |
| :--- | :--- | :--- |
| **Seungmin Yeom** | Team Leader | Project Management, Player Logic Implementation |
| **Seungju Yoon** | Developer | Login System, Store System, Enemy Logic |
| **Yubin Lee** | Developer | Boss Logic Implementation |
| **Taewoo Kim** | Developer | Item System Implementation |
| **Doyun Ji** | Resource Manager | External Resources (BGM, SFX) Search |

---

## 2. Configuration & Environment

This project is developed based on the following environment and libraries.

* **Language**: Java 21
* **Build Tool**: Gradle 8.7 (Kotlin DSL)
* **IDE**: IntelliJ IDEA

### 🛠 Key Dependencies
Major libraries defined in `build.gradle.kts`:

* **Test Framework**: JUnit 5 (v5.10.2)
    * `org.junit.jupiter:junit-jupiter-api`
    * `org.junit.jupiter:junit-jupiter-engine`
    * `org.junit.jupiter:junit-jupiter-params`
* **Mocking**: Mockito (v5.11.0 / Inline v5.2.0)
    * `org.mockito:mockito-core`
    * `org.mockito:mockito-junit-jupiter`
    * `org.mockito:mockito-inline` (Support for Static Method Mocking)
* **Code Coverage**: JaCoCo (v0.8.10)

### 📅 Project Management
* **JIRA (Kanban Board)**: [Project Link](https://sdp-25-pro9.atlassian.net/jira/software/projects/TP/boards/2)
* **Communication**: KakaoTalk, Slack (Meeting Minutes), Zoom (Ad-hoc meetings)
* **Regular Meeting**: Every Tuesday at 2:00 PM

### 🌿 Branch Strategy
We use a **Simplified Git Flow**. Since the team size is small, we aim for an efficient workflow instead of a complex structure.

1. `main`: Stable version branch ready for deployment.
2. `develop`: Integration branch where developing codes are merged.
3. `feature/*`: Individual workspace branches for feature implementation.
    * **Workflow**: `feature/task` → (PR & Merge) → `develop` → `main`

### 📝 Code Style
We follow the **Google Java Style Guide**.

* **Indentation**: 4 Spaces.
* **Comments**: Javadoc required for all methods (param, return, throws).
* **Naming Conventions**
    * Class: `PascalCase`
    * Variable, Method: `camelCase`
    * Variable: Nouns, No abbreviations (Use Full Names)
    * Method: Verbs
    * Constant: `UPPER_SNAKE_CASE` (e.g., `PI`, `MAX_SIZE`)
    * Branch: `feature/a-b` (lowercase)

### 🔏 Commit Convention
**"One feature per commit."**

1. **Subject**: `Tag: Subject` (e.g., `Feat: Add lazer type boss logic`)
2. **Body**: Explain **what** and **why** changed, rather than how.

### 🔄 PR Convention
* Request a PR when **one Task** under a single Epic is completed.

---

## 3. Getting Started

### Clone Repository
Download the project by entering the command below in your terminal.

```bash
git clone [https://github.com/invaders-sdp-pro9/Invaders-SDP-Pro9.git](https://github.com/invaders-sdp-pro9/Invaders-SDP-Pro9.git)
cd Invaders-SDP-Pro9
