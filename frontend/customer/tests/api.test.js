/**
 * API 모듈 테스트 - TC-FE-010 ~ TC-FE-013
 */
const ApiTests = {
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
            mockStorage.setItem('auth_token', 'test-token-123');
        }

        function restoreMocks() {
            window.fetch = originalFetch;
            localStorage.getItem = originalGetItem;
            localStorage.setItem = originalSetItem;
            localStorage.removeItem = originalRemoveItem;
        }

        // TC-FE-010: 성공 응답 처리
        await TestUtils.describeAsync('TC-FE-010: 성공 응답 처리', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                { status: 200, body: { categories: ['구이', '찌개'] }, headers: {} }
            ]);
            const result = await Api.request('GET', '/api/test');
            TestUtils.assertEqual(result.categories.length, 2, 'categories 2개');
            TestUtils.assertEqual(result.categories[0], '구이', '첫 번째 카테고리');
            restoreMocks();
        });

        // TC-FE-011: X-New-Token 헤더 감지 시 토큰 교체
        await TestUtils.describeAsync('TC-FE-011: X-New-Token 토큰 교체', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                { status: 200, body: { ok: true }, headers: { 'X-New-Token': 'new-token-456' } }
            ]);
            await Api.request('GET', '/api/test');
            TestUtils.assertEqual(mockStorage.getItem('auth_token'), 'new-token-456', '토큰 교체됨');
            restoreMocks();
        });

        // TC-FE-012: 401 응답 시 에러 throw
        await TestUtils.describeAsync('TC-FE-012: 401 에러 throw', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                { status: 401, body: { error: 'TOKEN_EXPIRED', message: '토큰 만료' }, headers: {} }
            ]);
            await TestUtils.assertRejects(
                () => Api.request('GET', '/api/test'),
                '401 시 에러 throw'
            );
            restoreMocks();
        });

        // TC-FE-013: 에러 응답 파싱
        await TestUtils.describeAsync('TC-FE-013: 에러 응답 파싱', async function () {
            setupMocks();
            window.fetch = TestUtils.createMockFetch([
                { status: 400, body: { error: 'VALIDATION_FAILED', message: '입력 오류' }, headers: {} }
            ]);
            try {
                await Api.request('POST', '/api/test', { data: 'bad' });
                TestUtils.assert(false, '에러가 throw되어야 함');
            } catch (e) {
                TestUtils.assertEqual(e.status, 400, 'status 400');
                TestUtils.assertEqual(e.errorCode, 'VALIDATION_FAILED', 'errorCode 일치');
                TestUtils.assertEqual(e.message, '입력 오류', 'message 일치');
            }
            restoreMocks();
        });
    }
};
