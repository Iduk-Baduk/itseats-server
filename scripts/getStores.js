import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100, // Virtual Users 수 (동시 접속자 수)
    duration: '60s', // 테스트 지속 시간
};

const BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    const res = http.get(`${BASE_URL}/api/stores/list`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1); // 각 요청 사이에 1초 대기
}
