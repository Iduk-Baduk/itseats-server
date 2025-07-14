<div align="center">
  
# **잇츠잇츠 API 서버**

<img height="120" alt="logo" src="https://github.com/user-attachments/assets/24711d5a-0280-4ed8-82f2-4f3227d65f4f" />
<br/>
<br/>

    쿠팡이츠 클론코딩 프로젝트

</div>
<br>


## 프로젝트 개요

1차 팀 프로젝트로 쿠팡이츠 서비스에 대한 클론코딩 프로젝트를 진행하였습니다.
고객, 가맹점, 라이더 세 가지 역할의 사용자에 대한 주문/배달 프로세스를 개발하였습니다.

<br/>

## 주요 기능

- 🍽️ 음식점 검색 및 메뉴 주문
- 🛒 장바구니 및 결제 시스템
- 💳 토스페이먼츠 연동 결제
- 🍕 주문 수락 및 주문 상태 관리
- 🛵 인근 배달 조회 및 배달 연계
- 🗺️ 주문 현황 추적
- ⭐ 리뷰 및 평점 시스템
- 🏷️ 쿠폰 시스템
- 📈 부하 테스트 및 모니터링
- ☁️ AWS 배포
<br/>
<img width="2167" height="955" alt="첨부용 사진" src="https://github.com/user-attachments/assets/06662d74-c947-4f00-8cf0-a3ae15598f6a" />
<br/>
<br/>

## 시스템 구조도

<img width="1588" height="1013" alt="시스템 구조도" src="https://github.com/user-attachments/assets/4751a9eb-eb89-48d6-9b7b-aedd77aefcb3" />

# 기술 스택

- **Backend**: Java 17, Spring Boot, Spring Data JPA
- **Authentication** : JWT, Spring Security
- **Database & Cache**: MySQL, Redis
- **Monitoring & Load Test**: K6, Prometheus, Grafana
- **CI/CD**: Github Actions
- **Infra & Deployment**: AWS Fargate, Docker

# 프로젝트 구조

```
src/main/java/com/idukbaduk/itseats/
├── external/
├── global/
├── member/
│   ├─── controller/
│   ├─── dto/
│   ├─── entity/
│   ├─── repository/
│   └─── service/
├── ...
└─── ItseatsServerApplication
```

# 링크
### 고객용 프론트엔드
[itseats-web-customer](https://github.com/Iduk-Baduk/itseats-web-customer)
### 가맹점용 프론트엔드
[itseats-web-customer](https://github.com/Iduk-Baduk/itseats-web-owner)
### 라이더용 프론트엔드
[itseats-web-customer](https://github.com/Iduk-Baduk/itseats-web-rider)

### Backend API Docs 
`http://localhost:8080/swagger-ui.html`


# 팀원

송준경 [@chk-jk](https://github.com/chk-jk)

김세은 [@sseen2](https://github.com/sseen2)

이우창 [@changi1122](https://github.com/changi1122)

정재환 [@jaehwannnnnn](https://github.com/jaehwannnnnn)

한상희 [@sanghee00](https://github.com/sanghee00)

박근석 [@parkrootseok](https://github.com/parkrootseok)
