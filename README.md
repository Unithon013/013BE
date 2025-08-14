# 🔥 불타는 씨니어, 불씨

**AI 기반 시니어 데이팅 앱 - 서버(Backend & AI)**

> AI 영상 분석을 통해 시니어의 자기소개를 프로필로 자동 완성하고,  
> 맞춤형 상대를 지능적으로 추천해주는 데이팅 앱의 백엔드 & AI 서버 프로젝트입니다.

<img src="https://ibb.co/dT6Ncpw">

---

## 🌟 주요 기능 (Key Features)

### 1. AI 기반 자동 프로필 생성

- 사용자가 **30초 자기소개 영상** 업로드 시, AI가 영상을 분석하여
  - 이름 / 나이 / 성별 / 취미 / 한 줄 소개 / 등
    을 **자동 추출** → 프로필 완성

### 2. 비동기 처리 시스템

- **STT**(Speech-to-Text) & **NLP** 분석을 백그라운드에서 실행
- **실시간 대기 없음** — 가입 절차 즉시 완료, 추후 프로필 자동 업데이트

### 3. 지능형 추천 알고리즘

- 단순 거리 기반 X
- **성별이 다르고**, **취미·나이가 비슷한** 상대 우선 추천
- **추천 제외 규칙**:
  1. 본인
  2. 오늘 이미 추천받은 사람
  3. 현재 채팅 중인 사람

### 4. 자연스러운 채팅 시작

- 추천받은 상대와 연락하기 시도 시 채팅방 자동 생성 후  
  → **첫 메시지로 내 자기소개 영상 자동 전송**

---

## 🧑‍💻 팀원 및 역할 (Team & Roles)

| 이름                                        | 역할     | 담당 파트                                             |
| ------------------------------------------- | -------- | ----------------------------------------------------- |
| [@김하빈](https://github.com/KIM-GOING)     | PM       | 사업 기획, 프로젝트 총괄                              |
| [@김민정](https://github.com/p1578p-debug)  | 디자이너 | UI/UX 디자인                                          |
| [@이영주](https://github.com/oortmealy)     | 개발자   | 프론트엔드 (React Native)                             |
| [@김은혜](https://github.com/eunhyekimyeah) | 개발자   | 프론트엔드 (React Native)                             |
| [@은지우](https://github.com/meraki6512)    | 개발자   | 백엔드 (Spring Boot), AI 서버 (FastAPI), 인프라 (AWS) |

---

## 🏗️ 시스템 아키텍처 (System Architecture)

본 프로젝트는 **메인 백엔드 서버**와 **AI 서버**의 **2-tier 구조**이며,  
`Docker Compose`로 AWS EC2 인스턴스에서 통합 운영됩니다.

`[React Native App] → [Spring Boot Backend] ↔ [FastAPI AI Server]`

### 📌 구성

1. **메인 백엔드 서버** (Spring Boot / Java)

   - 사용자 인증, DB 관리, 비즈니스 로직
   - AI 서버에 분석 요청 → 결과 polling → DB 업데이트

2. **AI 서버** (FastAPI / Python)

   - STT(Whisper), LLM(Gemini) 기반 프로필 생성
   - 백그라운드 작업 처리, task_id 기반 조회 API 제공

3. **인프라**
   - AWS EC2 t3.medium
   - RDS(MySQL)
   - Docker & Docker Compose

---

## 🛠 기술 스택 (Technology Stack)

**Backend Server**

- Spring Boot 3.x (Java 21)
- MySQL (AWS RDS)
- Spring Data JPA, RestTemplate

**AI Server**

- FastAPI (Python 3.11)
- STT: OpenAI Whisper (small)
- LLM: Google Gemini (gemini-1.5-flash)

**Infra**

- AWS EC2 (t3.medium)
- Docker, Docker Compose

---

## 💡 기술적 도전과 해결

| 문제                           | 원인                                              | 해결                                                                                                                                                                                                                                                        |
| ------------------------------ | ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| AI 분석 시간으로 인한 UX 저하  | Whisper & Gemini 분석 30초 소요                   | FastAPI `BackgroundTasks` + Spring `@Async`로 비동기 처리, 가입 즉시 완료                                                                                                                                                                                   |
| AI 서버 OOM(메모리 부족)       | Whisper 모델 로드 시 메모리 초과                  | EC2 → t3.medium 업그레이드(4GB RAM), 서버 시작 시 모델 사전 로드                                                                                                                                                                                            |
| 서버 간 통신 실패              | 컨테이너 시작 순서 문제                           | Docker `healthcheck` + `depends_on: service_healthy` 설정                                                                                                                                                                                                   |
| JPA 지연 로딩과 JSON 변환 충돌 | JPA의 Lazy Loading 기능으로 인한 JSON 직렬화 문제 | DTO를 통한 데이터 전달 설계, @Transactional이 보장된 상태에서 필요한 모든 데이터를 조회 -> 추가로 @JsonIgnore로 순환 참조 차단, Hibernate5Module 등록으로 초기화된 프록시만 노출, 서비스 계층에서 엔티티를 완전히 초기화하거나 fetch join 사용 등 고려 가능 |

---

## 📜 API 명세 (Endpoints)

### 📍 Backend Server (:8080)

| Method | Endpoint                        | 설명                      |
| ------ | ------------------------------- | ------------------------- |
| POST   | `/users/onboarding`             | 온보딩(가입) 요청         |
| GET    | `/users/me`                     | 내 정보 조회              |
| GET    | `/recommendations`              | 오늘의 추천 목록          |
| POST   | `/recommendations/additional`   | 포인트로 추가 추천        |
| POST   | `/chat/create/{targetUserId}`   | 채팅방 생성 + 첫영상 전송 |
| GET    | `/chat/rooms`                   | 내 채팅방 목록            |
| GET    | `/chat/rooms/{roomId}/messages` | 채팅방 메시지 내역        |

### 📍 AI Server (:8000)

| Method | Endpoint           | 설명                       |
| ------ | ------------------ | -------------------------- |
| POST   | `/process-video`   | 영상 분석 요청(백그라운드) |
| GET    | `/tasks/{task_id}` | 작업 상태/결과 조회        |
| GET    | `/health`          | 상태 확인                  |

---

## 🚀 설치 및 실행 방법 (Setup & Run)

### 1. EC2 준비

- Amazon Linux 2023, t3.medium 인스턴스
- Docker & Docker Compose 설치

### 2. 프로젝트 클론

`git clone <repo_url>`
`cd project_folder`

### 3. 환경 변수 설정

- `ai_server/.env` 에 **GOOGLE_API_KEY** 추가
- `docker-compose.yml` `environment` 섹션에 **RDS 접속 정보** 입력

### 4. 백엔드 빌드 & 업로드

`./gradlew bootJar`
`scp build/libs/backend.jar ec2-user@<EC2_IP>:/home/ec2-user/backend/`

### 5. 서버 실행

`sudo docker-compose up -d`

### 6. 로그 확인

`sudo docker-compose logs -f`

---

## 📌 아키텍처 다이어그램

[사용자] </br></br>
↓ (영상 업로드)</br></br>
[백엔드 서버] → DB 저장(상태: PROCESSING) → AI 서버에 분석 요청</br></br>
↓ (즉시 가입 완료 응답)</br></br>
[AI 서버] - Whisper(음성→텍스트) → Gemini(프로필 추출)</br></br>
↓ 결과 저장</br></br>
[백엔드 서버] 결과 polling 후 DB 업데이트 → 마이페이지 반영</br></br>

---

## 🏷 License

이 프로젝트는 팀 내부 포트폴리오 용도로 제작되었습니다.
