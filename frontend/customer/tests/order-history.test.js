/**
 * OrderHistory 모듈 테스트 - TC-FE-018 ~ TC-FE-020
 */
const OrderHistoryTests = {
    run: function () {
        // TC-FE-018: WAITING 상태 뱃지
        TestUtils.describe('TC-FE-018: WAITING 뱃지', function () {
            const badge = OrderHistoryView.getStatusBadge('WAITING');
            TestUtils.assertEqual(badge.text, '대기중', 'text 대기중');
            TestUtils.assertEqual(badge.cssClass, 'bg-warning', 'cssClass bg-warning');
        });

        // TC-FE-019: PREPARING 상태 뱃지
        TestUtils.describe('TC-FE-019: PREPARING 뱃지', function () {
            const badge = OrderHistoryView.getStatusBadge('PREPARING');
            TestUtils.assertEqual(badge.text, '준비중', 'text 준비중');
            TestUtils.assertEqual(badge.cssClass, 'bg-primary', 'cssClass bg-primary');
        });

        // TC-FE-020: DONE 상태 뱃지
        TestUtils.describe('TC-FE-020: DONE 뱃지', function () {
            const badge = OrderHistoryView.getStatusBadge('DONE');
            TestUtils.assertEqual(badge.text, '완료', 'text 완료');
            TestUtils.assertEqual(badge.cssClass, 'bg-success', 'cssClass bg-success');
        });
    }
};
