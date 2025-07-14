import http from 'k6/http';
import { check, group } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Counter } from 'k6/metrics';

export const options = {
    vus: 500,
    iterations: 1500,  // 500유저 * 3쿠폰 = 1500요청
};

const BASE_URL = 'http://host.docker.internal:8080';
const headers = { 'Content-Type': 'application/json' };

// 테스트할 쿠폰 ID들
const couponIds = [11, 12, 13];

// 쿠폰별 성공/실패 카운터
const successCounts = {};
const exceededCounts = {};
couponIds.forEach(id => {
    successCounts[id] = new Counter(`coupon_${id}_success_201`);
    exceededCounts[id] = new Counter(`coupon_${id}_exceeded_409`);
});

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

    couponIds.forEach(couponId => {
        group(`쿠폰 ${couponId} 발급 요청`, () => {
            const res = http.post(`${BASE_URL}/api/coupons/${couponId}/issue`, null, {
                headers: {
                    ...headers,
                    'Authorization': `Bearer ${accessToken}`,
                },
            });

            if (res.status === 201) {
                successCounts[couponId].add(1);
            } else if (res.status === 409) {
                exceededCounts[couponId].add(1);
            }

            const status = res.status;
            const isSuccess = status === 201;
            const isExceeded = status === 409 && res.body.includes('[COUPON ERROR] 쿠폰 발급 수량을 초과했습니다.');

            const label = `쿠폰 ${couponId} 응답 [${status}] | 발급 성공 (201) 또는 초과 오류 (409)`;
            check(res, {
                [label]: () => isSuccess || isExceeded,
            });

            console.log(`쿠폰 ${couponId} 응답: [${status}] ${res.body}`);
        });
    });
}

export function handleSummary(data) {
    let summary = '\n멀티 쿠폰 발급 테스트 요약\n─────────────────────────────────────────────\n';
    let totalRequests = 0;

    couponIds.forEach(couponId => {
        const success = data.metrics[`coupon_${couponId}_success_201`]?.values?.count || 0;
        const exceeded = data.metrics[`coupon_${couponId}_exceeded_409`]?.values?.count || 0;
        const total = success + exceeded;
        totalRequests += total;

        summary += `
쿠폰 ${couponId}
  201 Created (발급 성공)     : ${success}
  409 Exceeded (수량 초과)    : ${exceeded}
  총 요청 수                  : ${total}
  기대치 (quantity=100) 충족  : ${success === 100 && exceeded === (total - 100) ? '성공' : '실패'}
─────────────────────────────────────────────`;
    });

    summary += `\n총 발급 요청 수 (모든 쿠폰 합산) : ${totalRequests}\n─────────────────────────────────────────────\n`;

    return { stdout: summary };
}
