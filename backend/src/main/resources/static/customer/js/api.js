/**
 * API 통신 모듈 - 공통 Fetch Wrapper
 * JWT 인증, X-New-Token 자동 갱신, 에러 처리 중앙화
 */
const Api = (function () {
    const BASE_URL = '';

    class ApiError extends Error {
        constructor(status, errorCode, message) {
            super(message);
            this.status = status;
            this.errorCode = errorCode;
        }
    }

    async function request(method, endpoint, body) {
        const token = localStorage.getItem('auth_token');
        const headers = { 'Content-Type': 'application/json' };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const options = { method, headers };
        if (body && (method === 'POST' || method === 'PUT')) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(BASE_URL + endpoint, options);

        // X-New-Token 감지 → 토큰 자동 교체
        const newToken = response.headers.get('X-New-Token');
        if (newToken) {
            localStorage.setItem('auth_token', newToken);
        }

        const data = await response.json();

        if (!response.ok) {
            throw new ApiError(response.status, data.error || 'UNKNOWN', data.message || '오류가 발생했습니다');
        }

        return data;
    }

    async function get(endpoint) {
        return request('GET', endpoint, null);
    }

    async function post(endpoint, body) {
        return request('POST', endpoint, body);
    }

    return { BASE_URL, ApiError, request, get, post };
})();
