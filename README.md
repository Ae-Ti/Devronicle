# Devronicle

**개발자가 하루 끝에 "오늘 뭐 했더라" 를 다시 떠올리지 않게 한다.**

업무 일지는 이미 한 일을 다시 적는 일이다. 그런데 정작 무엇을 했는지는 저녁이 되면
잘 기억나지 않는다. 커밋 로그를 거슬러 보고, 열어 둔 PR 을 뒤지고, 아직 커밋하지 않은
변경을 더듬는다. 그렇게 20분을 쓰고 나온 일지는 대개 `수정` 세 줄이다.

Devronicle 은 그 흔적들이 **이미 어딘가 남아 있다**는 데서 출발한다. 커밋·PR·미커밋 변경·
편집기에 열어 둔 파일, 그리고 AI 에이전트와 나눈 대화까지 모아서 일지 초안을 만들어 준다.
사람은 고치고 확정만 한다.

```
GitHub  ─┐
          ├─→  수집  ─→  LLM 초안  ─→  사람이 고침  ─→  확정  ─→  채팅 알림
VS Code ─┘
```

---

## 세 덩어리로 되어 있다

| | 하는 일 |
|---|---|
| **VS Code 확장** | git 에 안 남는 구간을 본다 — 미커밋 변경, 미푸시 커밋, 아직 저장 안 한 파일, 오늘 계획, 그리고 **네 AI 도구**(Claude Code · 편집기 내장 채팅 · Codex · Gemini)의 대화 |
| **백엔드** | GitHub 활동을 모으고, LLM 으로 초안을 쓰고, 인증·권한을 지키고, 확정된 일지를 채팅으로 보낸다 |
| **대시보드** | 초안을 고쳐 확정한다. 관리자는 팀 전체의 활동과 일지를 본다 |

## 기술적으로 볼 만한 곳

**AI 대화를 네 도구에서 모은다** — [`vscode-extension/src/ai/`](vscode-extension/src/ai/)

도구마다 기록을 두는 곳도 형식도 다르다. 다른 것은 *어디 있고 어떤 모양인가* 둘뿐이라,
그 둘만 `AiSource` 인터페이스 뒤로 숨겼다. 몇 개를 담을지·몇 자에서 자를지 같은 규칙은
한 곳에 남아 도구가 늘어도 갈라지지 않는다. 편집기 없이 도는 테스트로 실제 기록 파일까지
훑는다.

**설정을 사람이 나르지 않는다** — [`vscode-extension/src/connect.ts`](vscode-extension/src/connect.ts)

확장이 서버로 보내려면 백엔드 주소와 개인 키가 둘 다 있어야 한다. 처음엔 사람이 대시보드에서
키를 복사해 편집기로 건너와 주소까지 적었다 — 여섯 걸음이고, 주소를 잘못 적으면 애먼 키를
의심하게 된다. 두 값을 모두 아는 쪽은 대시보드이므로 옮기는 일도 대시보드가 하게 했다.
`vscode://` 딥링크 한 번이면 주소와 키가 함께 들어가고, 곧바로 한 번 보내 연결까지 확인한다.
링크가 막히는 자리(원격 접속·브라우저 차단)를 위해 붙여넣기 한 줄짜리 연결 코드도 같이 준다.
키는 설정 파일이 아니라 편집기 비밀 저장소(OS 키체인)에 둔다.

**사내망인 척하는 Origin 을 가른다** — [`backend/src/main/java/com/worklog/config/InternalNetwork.java`](backend/src/main/java/com/worklog/config/InternalNetwork.java)

사람마다 접속 주소가 달라 사설 IP 대역을 열어야 했다. 그런데 Spring 의 Origin 패턴에서
`*` 는 점까지 삼켜서, `http://192.168.*` 이 `192.168.0.10.evil.com` 까지 통과시킨다.
남이 그런 이름을 잡아 두면 사내망인 척 들어온다. 그래서 패턴 대신 **네 칸짜리 숫자 주소인지**
까지 확인하고, 통과하면 그 주소 하나만 허용한다.

**키는 해시만 남긴다** — [`backend/src/main/java/com/worklog/auth/ApiKeyService.java`](backend/src/main/java/com/worklog/auth/ApiKeyService.java)

평문은 발급 응답에서 한 번만 보여 주고 DB 에는 SHA-256 해시만 둔다. 키 자체가 32바이트
난수라 사전 공격 대상이 아니어서, 매 요청 검증에 쓰이는 값에 느린 해시를 쓰지 않았다.

## 기술 스택

| | |
|---|---|
| 백엔드 | Java 21 · Spring Boot 3.3 · Spring Security · JPA · Flyway · PostgreSQL 16 |
| 프론트 | React 18 · TypeScript · Vite · Tailwind · TanStack Query · Recharts |
| 확장 | TypeScript · VS Code Extension API (`vsce`) |
| 그 밖 | Docker Compose · OpenAI 호환 LLM · GitHub OAuth · Mattermost |

Java 132 파일 · 프론트 74 파일 · 확장 15 파일. 마이그레이션 15개.
**테스트 341개** — 백엔드 309, 확장 32.

## 돌려 보기

JDK 21 과 Docker Desktop 이 필요하다.

```bash
./scripts/setup.sh          # .env 생성 · 비밀값 발급 · DB 기동 · 의존성 설치
./scripts/backend.sh        # http://localhost:8080/api
cd frontend && npm run dev  # http://localhost:5173
```

`admin / admin1234` 로 들어간다. 첫 로그인에서 비밀번호를 바꾸게 한다.

VS Code 확장은 빌드하지 않고 바로 깔 수 있다.

```bash
code --install-extension vscode-extension/release/worklog-drafter-0.5.0.vsix
```

확장은 편집기 안에서 **WorkLog Drafter** 라는 이름으로 보인다.

깐 뒤 대시보드 → 설정 → API 연동 → **새 키 발급** → **`VS Code 에 연결`**.
옮겨 적을 것은 없다.

자세한 것은 [개발 안내](docs/DEVELOPMENT.md) 와 [SETUP](docs/SETUP.md) 에 있다.

## 맡은 부분

3일 동안 둘이 나눠 만든 협업 프로젝트다. 트랙을 갈라 맡았고, 내 트랙은 **클라이언트**였다.

- `frontend/` — 대시보드 전체 (초안 편집, 설정, 관리자 콘솔 화면)
- `vscode-extension/` — 확장 전부 (수집기, 사이드바, AI 대화 파서, 연결 흐름)
- 백엔드에서는 초안 CRUD 와 확장이 보고하는 경로, 그리고 그 경로들의 권한 검사

## 문서

만드는 동안 남긴 기록을 그대로 뒀다 — PRD, 날마다의 계획과 정리, 인수인계, 설계 결정.
무엇을 왜 그렇게 정했는지가 [`docs/`](docs/) 에 있다.

---

공개용으로 옮기며 사내 주소·조직명·연동 시스템 이름은 중립적인 값으로 바꿨다.
설정 값은 모두 `.env.example` 에만 두고 실제 값은 저장소에 넣지 않는다.
