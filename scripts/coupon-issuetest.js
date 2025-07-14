import http from 'k6/http';
import { check, group } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Counter } from 'k6/metrics';

export const options = {
    vus: 500,
    iterations: 500,
};

const BASE_URL = 'http://host.docker.internal:8080';
const headers = { 'Content-Type': 'application/json' };

// 발급 성공/초과 카운터
const successCount = new Counter('coupon_success_201');
const exceededCount = new Counter('coupon_exceeded_409');

export default function () {
    const suffix = uuidv4();
    const username = `loaduser_${suffix}`;
    const password = 'Password123!';
    const email = `${username}@test.com`;

    let accessToken = '';

    group('회원가입', () => {
        const payload = JSON.stringify({
            username,
            password,
            name: 'LoadTester',
            nickname: username,
            email,
            phone: '010-1234-5678',
        });

        const res = http.post(`${BASE_URL}/api/members/sign-up`, payload, { headers });
        check(res, {
            '회원가입 응답코드 201 또는 409': (r) => r.status === 201 || r.status === 409,
        });
    });

    group('로그인 및 토큰 획득', () => {
        const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
            username,
            password,
        }), { headers });

        accessToken = res.headers['Access-Token'];

        check(res, {
            '로그인 응답 200': (r) => r.status === 200,
            'accessToken 존재': () => !!accessToken,
        });
    });

    group('선착순 쿠폰 발급 요청', () => {
        const res = http.post(`${BASE_URL}/api/coupons/6/issue`, null, {
            headers: {
                ...headers,
                'Authorization': `Bearer ${accessToken}`,
            },
        });

        if (res.status === 201) {
            successCount.add(1);
        } else if (res.status === 409) {
            exceededCount.add(1);
        }

        const status = res.status;
        const isSuccess = status === 201;
        const isExceeded = status === 409 && res.body.includes('[COUPON ERROR] 쿠폰 발급 수량을 초과했습니다.');

        const label = `쿠폰 응답 [${status}] | 발급 성공 (201) 또는 초과 오류 (409)`;
        check(res, {
            [label]: () => isSuccess || isExceeded,
        });

        console.log(`응답: [${status}] ${res.body}`);
    });
}

export function handleSummary(data) {
    const success = data.metrics['coupon_success_201']?.values?.count || 0;
    const exceeded = data.metrics['coupon_exceeded_409']?.values?.count || 0;
    const total = success + exceeded;

    return {
        stdout: `
        선착순 쿠폰 발급 테스트 요약
        ────────────────────────────────────────────
        201 Created (발급 성공)     : ${success}
        409 Exceeded (수량 초과)    : ${exceeded}
        총 발급 요청 수             : ${total}
        기대치 (quantity=100) 충족  : ${success === 100 && exceeded === (total - 100) ? '성공' : '실패'}
        ────────────────────────────────────────────
        `,
    };
}