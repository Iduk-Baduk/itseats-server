import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 300, // Virtual Users 수 (동시 접속자 수)
    duration: '60s', // 테스트 지속 시간
};

const AUTH_TOKEN = 'Bearer eyJ0eXBlIjoiQUNDRVNTX1RPS0VOIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiIxIiwiaWF0IjoxNzUyNDgwMjEyLCJleHAiOjE3NTI0ODM4MTJ9.QEXj4fmKKS8DeWdcopUavpcYePktlZY0R6NmDAyVOVFuVqVMH6gEJANY2gWiDwjiWTAetOJXCqVCxYQUWZWTsg';

const params = {
    headers: {
        Authorization: AUTH_TOKEN,
    },
};

const BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    const res = http.get(`${BASE_URL}/api/stores/list/korean?sort=DISTANCE&addressId=1`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1); // 각 요청 사이에 1초 대기
}