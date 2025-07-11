import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,             // 1명 사용자
    duration: '15s'
};

const BASE_URL = 'http://host.docker.internal:8080';  // Spring Boot 실행 주소

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'health is UP': (r) => r.body && r.body.includes('"status":"UP"'),
    });

    sleep(1);
}
