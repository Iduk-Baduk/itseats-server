# 라이더(RIDER) API 명세

> 모든 라이더 API는 JWT 인증이 필요하며, 실제 서비스에서는 memberType이 `RIDER`인 사용자만 접근해야 합니다.

---

## 공통 안내
- **인증:** 모든 요청은 `Authorization: Bearer <JWT>` 헤더 필요
- **권한:** 서버에서 반드시 memberType이 `RIDER`인지 체크 필요
- **응답 포맷:**
  ```json
  {
    "httpStatus": 200,
    "message": "성공 메시지",
    "data": { /* 실제 데이터 */ }
  }
  ```
- **에러 예시:**
  ```json
  {
    "httpStatus": 403,
    "message": "권한이 없습니다.",
    "data": null
  }
  ```

---

## 1. 출/퇴근 상태 변경
- **URL:** `/api/rider/working`
- **Method:** `POST`
- **설명:** 라이더의 출근/퇴근 상태를 변경합니다.
- **Request Body:**
  | 필드명     | 타입    | 필수 | 설명         |
  |----------|-------|-----|------------|
  | isWorking | boolean | Y   | true: 출근, false: 퇴근 |
  
  예시:
  ```json
  {
    "isWorking": true
  }
  ```
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "출/퇴근 상태전환 성공",
    "data": {
      "isWorking": true
    }
  }
  ```

---

## 2. 배달 거절
- **URL:** `/api/rider/{orderId}/reject`
- **Method:** `PUT`
- **설명:** 라이더가 배달을 거절합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Request Body:**
  | 필드명        | 타입   | 필수 | 설명     |
  |-------------|------|-----|--------|
  | rejectReason | String | Y   | 거절 사유 |
  
  예시:
  ```json
  {
    "rejectReason": "고장"
  }
  ```
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "배달 거절 완료",
    "data": {
      "rejectReason": "고장"
    }
  }
  ```

---

## 3. 배정받은 주문 상세 조회
- **URL:** `/api/rider/{orderId}/details`
- **Method:** `GET`
- **설명:** 라이더가 배정받은 주문의 상세 정보를 조회합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "주문 상세 조회 성공",
    "data": {
      "orderId": 123,
      "storeName": "BHC 구름점",
      "orderPrice": 18000,
      "orderStatus": "WAITING",
      "address": "서울시 ...",
      ...
    }
  }
  ```

---

## 4. 배달 수락
- **URL:** `/api/rider/{orderId}/accept`
- **Method:** `PUT`
- **설명:** 라이더가 배달을 수락합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "배달 수락 완료",
    "data": null
  }
  ```

---

## 5. 매장 도착 처리
- **URL:** `/api/rider/{orderId}/arrived`
- **Method:** `PUT`
- **설명:** 라이더가 매장에 도착했음을 처리합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "매장 도착 완료",
    "data": null
  }
  ```

---

## 6. 픽업 처리
- **URL:** `/api/rider/{orderId}/pickup`
- **Method:** `PUT`
- **설명:** 라이더가 음식을 픽업했음을 처리합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "픽업 완료",
    "data": null
  }
  ```

---

## 7. 배달 인증 사진 업로드
- **URL:** `/api/rider/{orderId}/picture`
- **Method:** `POST`
- **Content-Type:** `multipart/form-data`
- **설명:** 라이더가 배달 인증 사진을 업로드합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Request Param:**
  | 이름   | 타입   | 필수 | 설명         |
  |------|------|-----|------------|
  | image | file | Y   | 이미지 파일 |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "배달 인증 사진 업로드 성공",
    "data": {
      "imageUrl": "https://..."
    }
  }
  ```

---

## 8. 배달 완료 처리
- **URL:** `/api/rider/{orderId}/done`
- **Method:** `PUT`
- **설명:** 라이더가 배달을 완료했음을 처리합니다.
- **Path Variable:**
  | 이름      | 타입  | 필수 | 설명     |
  |---------|-----|-----|--------|
  | orderId | Long | Y   | 주문 ID |
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "배달 완료",
    "data": null
  }
  ```

---

## 9. 배정 대기 주문 목록 조회
- **URL:** `/api/rider/request`
- **Method:** `GET`
- **설명:** 라이더가 배정 대기 중인 주문 목록을 조회합니다.
- **Response 예시:**
  ```json
  {
    "httpStatus": 200,
    "message": "대기 배달 주문 조회 성공",
    "data": [
      {
        "orderId": 123,
        "storeName": "BHC 구름점",
        "orderPrice": 18000,
        "address": "서울시 ...",
        ...
      },
      ...
    ]
  }
  ```

---

## 공통 에러/권한 안내
- 401 Unauthorized: 토큰 만료/없음/비정상 → 로그인 필요
- 403 Forbidden: 권한 없음(라이더가 아님 등)
- 500: 서버 오류

---

## 참고
- 실제 데이터 구조는 서버 구현에 따라 일부 달라질 수 있습니다.
- memberType이 `RIDER`가 아닌 경우, 서버에서 403 에러를 반환해야 합니다. 
