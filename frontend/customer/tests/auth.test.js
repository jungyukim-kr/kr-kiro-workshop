/**
 * Auth 모듈 테스트 - TC-FE-014 ~ TC-FE-017
 */
const AuthTests = {
    run: async function () {
        const originalFetch = window.fetch;
        const mockStorage = TestUtils.createMockStorage();
        const originalGetItem = localStorage.getItem;
        const originalSetItem = localStorage.setItem;
        const originalRemoveItem = localStorage.removeItem;

        function setupMocks() {
            localStorage.getItem = mockStorage.getItem;
            localStorage.setItem = mockStorage.setItem;
            localStorage.removeItem = mockStorage.removeItem;
            mockStorage.clear();
        }

        function restoreMocks() {
            window.fetch = originalFetch;
            localStorage.getItem = originalGetItem;
            localStorage.setItem = originalSetItem;
            localStorage.removeItem = originalRemoveItem;
        }

        // TC-FE-014: 로그인 성공 시 localStorage 저장
        await TestUtils.describeAsync('TC-FE-014: 로그인 성공 localStorage 저장', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                {
                    status: 200,
                    body: { token: 'jwt-token-abc', storeId: 1, role: 'TABLE', userId: 5, tableId: 5, sessionId: 10 },
                    headers: {}
                }
            ]);
            await Auth.login('STORE001', 5, '1234');
            TestUtils.assertEqual(mockStorage.getItem('auth_token'), 'jwt-token-abc', 'token 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_storeId'), '1', 'storeId 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_tableId'), '5', 'tableId 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_sessionId'), '10', 'sessionId 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_storeCode'), 'STORE001', 'storeCode 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_tableNumber'), '5', 'tableNumber 저장');
            TestUtils.assertEqual(mockStorage.getItem('auth_password'), '1234', 'password 저장');
            restoreMocks();
        });

        // TC-FE-015: 로그인 실패 시 에러 throw
        await TestUtils.describeAsync('TC-FE-015: 로그인 실패 에러', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                { status: 401, body: { error: 'AUTHENTICATION_FAILED', message: '로그인 실패' }, headers: {} }
            ]);
            await TestUtils.assertRejects(
                () => Auth.login('STORE001', 5, '0000'),
                '로그인 실패 시 에러 throw'
            );
            restoreMocks();
        });

        // TC-FE-016: 토큰 있으면 isLoggedIn() true
        TestUtils.describe('TC-FE-016: isLoggedIn true', function () {
            setupMocks();
            mockStorage.setItem('auth_token', 'some-token');
            TestUtils.assertEqual(Auth.isLoggedIn(), true, '토큰 있으면 true');
            restoreMocks();
        });

        // TC-FE-017: 토큰 없으면 isLoggedIn() false
        TestUtils.describe('TC-FE-017: isLoggedIn false', function () {
            setupMocks();
            TestUtils.assertEqual(Auth.isLoggedIn(), false, '토큰 없으면 false');
            restoreMocks();
        });
    }
};
