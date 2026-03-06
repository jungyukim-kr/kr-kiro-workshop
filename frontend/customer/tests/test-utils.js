/**
 * 테스트 유틸리티 - 간단한 assert + mock localStorage + mock fetch
 */
const TestUtils = (function () {
    let passed = 0;
    let failed = 0;
    const results = [];

    function assert(condition, message) {
        if (condition) {
            passed++;
            results.push({ status: 'PASSED', message });
            console.log(`  ✓ ${message}`);
        } else {
            failed++;
            results.push({ status: 'FAILED', message });
            console.error(`  ✗ ${message}`);
        }
    }

    function assertEqual(actual, expected, message) {
        assert(actual === expected, `${message} (expected: ${expected}, actual: ${actual})`);
    }

    function assertThrows(fn, message) {
        try {
            fn();
            assert(false, `${message} - expected error but none thrown`);
        } catch (e) {
            assert(true, message);
        }
    }

    async function assertRejects(asyncFn, message) {
        try {
            await asyncFn();
            assert(false, `${message} - expected rejection but resolved`);
        } catch (e) {
            assert(true, message);
        }
    }

    function describe(suiteName, fn) {
        console.log(`\n━━━ ${suiteName} ━━━`);
        fn();
    }

    async function describeAsync(suiteName, fn) {
        console.log(`\n━━━ ${suiteName} ━━━`);
        await fn();
    }

    function summary() {
        console.log(`\n════════════════════════════`);
        console.log(`Total: ${passed + failed}, Passed: ${passed}, Failed: ${failed}`);
        console.log(`════════════════════════════`);
        return { passed, failed, total: passed + failed, results };
    }

    function reset() {
        passed = 0;
        failed = 0;
        results.length = 0;
    }

    // Mock localStorage
    function createMockStorage() {
        const store = {};
        return {
            getItem: (key) => store[key] || null,
            setItem: (key, val) => { store[key] = String(val); },
            removeItem: (key) => { delete store[key]; },
            clear: () => { Object.keys(store).forEach(k => delete store[k]); },
            _store: store
        };
    }

    // Mock fetch
    function createMockFetch(responses) {
        let callIndex = 0;
        const calls = [];

        function mockFetch(url, options) {
            const call = { url, options };
            calls.push(call);
            const resp = responses[callIndex] || responses[responses.length - 1];
            callIndex++;

            const headers = new Map(Object.entries(resp.headers || {}));
            return Promise.resolve({
                ok: resp.status >= 200 && resp.status < 300,
                status: resp.status,
                headers: {
                    get: (name) => headers.get(name) || null
                },
                json: () => Promise.resolve(resp.body)
            });
        }

        mockFetch.calls = calls;
        mockFetch.reset = () => { callIndex = 0; calls.length = 0; };
        return mockFetch;
    }

    return {
        assert, assertEqual, assertThrows, assertRejects,
        describe, describeAsync, summary, reset,
        createMockStorage, createMockFetch
    };
})();
