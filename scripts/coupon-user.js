import http from 'k6/http';
import { check, group, sleep } from 'k6';

export const options = {
    vus: 1,
    iterations: 1,
};

const suffix = Date.now();
const username = `k6user_${suffix}`;
const password = 'Password123!';
const email = `${username}@test.com`;
const BASE_URL = 'http://host.docker.internal:8080';
const headers = { 'Content-Type': 'application/json' };

export default function () {
    let accessToken = '';

    group('회원가입', () => {

        const payload = JSON.stringify({
            username: username,
            password: password,
            name: 'K6Tester',
            nickname: username,
            email: email,
            phone: '010-1234-5678'
        });

        const res = http.post(`${BASE_URL}/api/members/sign-up`, payload, { headers });

        check(res, {
            '회원가입 응답코드가 201 또는 409': (r) => r.status === 201 || r.status === 409,
        });
    });

    group('로그인 및 토큰 획득', () => {
        const res = http.post(`${BASE_URL}/api/login`, JSON.stringify({
            username,
            password,
        }), { headers });

        check(res, {
            '로그인 성공 (200)': (r) => r.status === 200,
        });

        for (const key in res.headers) {
            if (key.toLowerCase() === 'access-token') {
                accessToken = res.headers[key];
                break;
            }
        }

        check(accessToken, {
            'accessToken 헤더에서 추출됨': (t) => !!t,
        });
    });

    group('쿠폰 발급 10회 시도', () => {
        for (let i = 1; i <= 10; i++) {
            const res = http.post(`${BASE_URL}/api/coupons/1/issue`, null, {
                headers: {
                    ...headers,
                    'Authorization': `Bearer ${accessToken}`,
                },
            });

            const status = res.status;
            const isCreated = res.status === 201;
            const isDuplicate =
                res.status === 409 &&
                (res.message('이미 발급받은 쿠폰입니다') || res.body.includes('ALREADY_ISSUED'));

            const label = `[${i}] 쿠폰 응답 [${status}] | 발급 성공 (201) 또는 중복 오류 (409)`;

            check(res, {
                [label]: () => isCreated || isDuplicate,
            });

            console.log(`${label} / ${res.body}`);
        }
    });
}
