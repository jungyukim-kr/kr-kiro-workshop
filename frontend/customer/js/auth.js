/**
 * 인증 모듈 - 로그인, 자동 로그인, 토큰 관리
 */
const Auth = (function () {
    const KEYS = {
        storeCode: 'auth_storeCode',
        tableNumber: 'auth_tableNumber',
        password: 'auth_password',
        token: 'auth_token',
        storeId: 'auth_storeId',
        tableId: 'auth_tableId',
        sessionId: 'auth_sessionId'
    };

    async function init() {
        if (!isLoggedIn()) {
            // 저장된 credentials로 재로그인 시도
            const storeCode = localStorage.getItem(KEYS.storeCode);
            const tableNumber = localStorage.getItem(KEYS.tableNumber);
            const password = localStorage.getItem(KEYS.password);
            if (storeCode && tableNumber && password) {
                try {
                    await login(storeCode, Number(tableNumber), password);
                    return true;
                } catch (e) {
                    clearAuth();
                    return false;
                }
            }
            return false;
        }
        // 토큰 유효성 확인
        const result = await validateToken();
        if (result) return true;
        // validate 실패 → 저장된 credentials로 재로그인
        const storeCode = localStorage.getItem(KEYS.storeCode);
        const tableNumber = localStorage.getItem(KEYS.tableNumber);
        const password = localStorage.getItem(KEYS.password);
        if (storeCode && tableNumber && password) {
            try {
                await login(storeCode, Number(tableNumber), password);
                return true;
            } catch (e) {
                clearAuth();
                return false;
            }
        }
        clearAuth();
        return false;
    }

    async function login(storeCode, tableNumber, password) {
        const response = await Api.post('/api/auth/table/login', {
            storeCode: storeCode,
            tableNumber: tableNumber,
            password: String(password)
        });
        // localStorage에 저장
        localStorage.setItem(KEYS.storeCode, storeCode);
        localStorage.setItem(KEYS.tableNumber, String(tableNumber));
        localStorage.setItem(KEYS.password, String(password));
        localStorage.setItem(KEYS.token, response.token);
        localStorage.setItem(KEYS.storeId, String(response.storeId));
        localStorage.setItem(KEYS.tableId, String(response.tableId));
        if (response.sessionId != null) {
            localStorage.setItem(KEYS.sessionId, String(response.sessionId));
        }
        return response;
    }

    async function validateToken() {
        try {
            const result = await Api.get('/api/auth/validate');
            return result;
        } catch (e) {
            return null;
        }
    }

    function getToken() {
        return localStorage.getItem(KEYS.token);
    }

    function getStoreId() {
        const val = localStorage.getItem(KEYS.storeId);
        return val ? Number(val) : null;
    }

    function getSessionId() {
        const val = localStorage.getItem(KEYS.sessionId);
        return val ? Number(val) : null;
    }

    function updateSessionId(sessionId) {
        if (sessionId != null) {
            localStorage.setItem(KEYS.sessionId, String(sessionId));
        }
    }

    function clearAuth() {
        Object.values(KEYS).forEach(k => localStorage.removeItem(k));
    }

    function isLoggedIn() {
        return !!localStorage.getItem(KEYS.token);
    }

    return { KEYS, init, login, validateToken, getToken, getStoreId, getSessionId, updateSessionId, clearAuth, isLoggedIn };
})();
