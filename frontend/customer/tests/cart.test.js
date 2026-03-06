/**
 * Cart 모듈 테스트 - TC-FE-001 ~ TC-FE-009
 */
const CartTests = {
    run: function () {
        // Mock localStorage
        const mockStorage = TestUtils.createMockStorage();
        const originalGetItem = localStorage.getItem;
        const originalSetItem = localStorage.setItem;
        const originalRemoveItem = localStorage.removeItem;

        function setupMockStorage() {
            localStorage.getItem = mockStorage.getItem;
            localStorage.setItem = mockStorage.setItem;
            localStorage.removeItem = mockStorage.removeItem;
            mockStorage.clear();
        }

        function restoreStorage() {
            localStorage.getItem = originalGetItem;
            localStorage.setItem = originalSetItem;
            localStorage.removeItem = originalRemoveItem;
        }

        // TC-FE-001: 빈 장바구니에 메뉴 추가 시 수량 1로 추가
        TestUtils.describe('TC-FE-001: 빈 장바구니에 메뉴 추가', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            const items = Cart.getItems();
            TestUtils.assertEqual(items.length, 1, '장바구니 항목 1개');
            TestUtils.assertEqual(items[0].quantity, 1, '수량 1');
            TestUtils.assertEqual(items[0].menuId, 1, 'menuId 일치');
            TestUtils.assertEqual(items[0].menuName, '김치찌개', 'menuName 일치');
            TestUtils.assertEqual(items[0].unitPrice, 9000, 'unitPrice 일치');
            restoreStorage();
        });

        // TC-FE-002: 이미 있는 메뉴 추가 시 수량 +1
        TestUtils.describe('TC-FE-002: 기존 메뉴 수량 증가', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            const items = Cart.getItems();
            TestUtils.assertEqual(items.length, 1, '항목 수 여전히 1개');
            TestUtils.assertEqual(items[0].quantity, 2, '수량 2로 증가');
            restoreStorage();
        });

        // TC-FE-003: 수량 +1 증가
        TestUtils.describe('TC-FE-003: updateQuantity +1', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.updateQuantity(1, 1);
            TestUtils.assertEqual(Cart.getItemQuantity(1), 3, '수량 3');
            restoreStorage();
        });

        // TC-FE-004: 수량 -1 감소
        TestUtils.describe('TC-FE-004: updateQuantity -1', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.updateQuantity(1, -1);
            TestUtils.assertEqual(Cart.getItemQuantity(1), 1, '수량 1로 감소');
            restoreStorage();
        });

        // TC-FE-005: 수량 0이면 자동 제거
        TestUtils.describe('TC-FE-005: 수량 0 자동 제거', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.updateQuantity(1, -1);
            TestUtils.assertEqual(Cart.getItems().length, 0, '항목 제거됨');
            restoreStorage();
        });

        // TC-FE-006: 항목 삭제
        TestUtils.describe('TC-FE-006: removeItem', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 2, name: '된장찌개', price: 8000, hasSpicyOptions: false });
            Cart.removeItem(1);
            const items = Cart.getItems();
            TestUtils.assertEqual(items.length, 1, '1개 남음');
            TestUtils.assertEqual(items[0].menuId, 2, '된장찌개만 남음');
            restoreStorage();
        });

        // TC-FE-007: 장바구니 비우기
        TestUtils.describe('TC-FE-007: clear', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 2, name: '된장찌개', price: 8000, hasSpicyOptions: false });
            Cart.addItem({ id: 3, name: '삼겹살', price: 13000, hasSpicyOptions: false });
            Cart.clear();
            TestUtils.assertEqual(Cart.getItems().length, 0, '비어있음');
            TestUtils.assertEqual(Cart.getTotalAmount(), 0, '총 금액 0');
            restoreStorage();
        });

        // TC-FE-008: 총 금액 계산
        TestUtils.describe('TC-FE-008: getTotalAmount', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 2, name: '삼겹살', price: 13000, hasSpicyOptions: false });
            TestUtils.assertEqual(Cart.getTotalAmount(), 31000, '9000*2 + 13000*1 = 31000');
            restoreStorage();
        });

        // TC-FE-009: 총 수량 계산
        TestUtils.describe('TC-FE-009: getTotalCount', function () {
            setupMockStorage();
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 1, name: '김치찌개', price: 9000, hasSpicyOptions: true });
            Cart.addItem({ id: 2, name: '삼겹살', price: 13000, hasSpicyOptions: false });
            TestUtils.assertEqual(Cart.getTotalCount(), 3, '2 + 1 = 3');
            restoreStorage();
        });
    }
};
