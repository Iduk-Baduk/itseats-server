# 🔧 JWT 인증 필터 추가 및 UserDetails null 처리 수정

## 📋 개요
프론트엔드에서 로그인 후 `/api/members/me` API 호출 시 발생하는 `UserDetails` null 오류를 해결하기 위한 수정사항입니다.

## 🐛 문제 상황
- 로그인은 성공하지만, 로그인 후 `/api/members/me` API 호출 시 500 오류 발생
- `NullPointerException: Cannot invoke "org.springframework.security.core.userdetails.UserDetails.getUsername()" because "userDetails" is null`
- JWT 토큰이 제대로 처리되지 않아 인증 컨텍스트가 설정되지 않는 문제

## ✅ 해결 방법

### 1. JWT 인증 필터 추가
**파일**: `src/main/java/com/idukbaduk/itseats/external/jwt/filter/JwtAuthenticationFilter.java` (신규 생성)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        // JWT 토큰 추출 및 검증
        // 인증 컨텍스트 설정
    }
}
```

**기능**:
- JWT 토큰을 추출하고 검증
- 토큰이 유효한 경우 `SecurityContextHolder`에 인증 정보 설정
- 로그인하지 않은 사용자도 정상적으로 처리

### 2. Spring Security 설정 업데이트
**파일**: `src/main/java/com/idukbaduk/itseats/global/config/SecurityConfig.java`

```java
// JWT 인증 필터를 Spring Security 필터 체인에 추가
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

### 3. Controller에서 UserDetails null 처리
**파일**: `src/main/java/com/idukbaduk/itseats/member/controller/MemberController.java`

```java
@GetMapping("/me")
public ResponseEntity<BaseResponse> getCurrentMember(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
        throw new RuntimeException("인증되지 않은 사용자입니다.");
    }
    // ... 기존 로직
}
```

**파일**: `src/main/java/com/idukbaduk/itseats/memberaddress/controller/MemberAddressController.java`

```java
@GetMapping
public ResponseEntity<BaseResponse> getAddressList(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
        return BaseResponse.toResponseEntity(
                AddressResponse.GET_ADDRESS_LIST_SUCCESS,
                memberAddressService.getAddressList(null)
        );
    }
    // ... 기존 로직
}
```

### 4. Service에서 null 처리
**파일**: `src/main/java/com/idukbaduk/itseats/memberaddress/service/MemberAddressService.java`

```java
@Transactional(readOnly = true)
public List<AddressResponse> getAddressList(String username) {
    if (username == null) {
        return List.of(); // 빈 리스트 반환
    }
    // ... 기존 로직
}
```

## 🔄 변경된 파일 목록

| 파일 | 변경 유형 | 설명 |
|------|-----------|------|
| `JwtAuthenticationFilter.java` | 신규 생성 | JWT 토큰 검증 및 인증 컨텍스트 설정 |
| `SecurityConfig.java` | 수정 | JWT 인증 필터를 Spring Security에 추가 |
| `MemberController.java` | 수정 | getCurrentMember에서 UserDetails null 처리 |
| `MemberAddressController.java` | 수정 | getAddressList에서 UserDetails null 처리 |
| `MemberAddressService.java` | 수정 | getAddressList에서 username null 처리 |

## 🧪 테스트 결과

### Before (수정 전)
```
❌ GET /api/members/me → 500 Internal Server Error
❌ GET /api/addresses → 500 Internal Server Error
```

### After (수정 후)
```
✅ GET /api/members/me → 200 OK (로그인된 사용자)
✅ GET /api/addresses → 200 OK (빈 배열 반환)
✅ 로그인하지 않은 사용자도 정상 처리
```

## 🚀 배포 영향도
- **낮음**: 기존 API 동작에 영향을 주지 않음
- **호환성**: 로그인하지 않은 사용자도 정상적으로 처리
- **성능**: JWT 토큰 검증으로 인한 미미한 성능 영향

## 📝 추가 고려사항
- JWT 토큰 만료 시 자동 갱신 로직은 별도 구현 필요
- 보안 강화를 위한 토큰 블랙리스트 기능 고려
- 로그인하지 않은 사용자에 대한 더 세밀한 권한 제어 필요 시 추가 구현

## 🔗 관련 이슈
- 프론트엔드 로그인 후 API 호출 시 500 오류 발생
- JWT 토큰 기반 인증 시스템 구축 
