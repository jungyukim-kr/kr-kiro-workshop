/**
 * 주문 내역 모듈 - 주문 조회, 무한 스크롤, 상태 뱃지
 */
const OrderHistoryView = (function () {
    let currentPage = 0;
    let hasMore = true;
    let isLoading = false;
    let observer = null;

    function render() {
        currentPage = 0; hasMore = true; isLoading = false;
        const listEl = document.getElementById('order-history-list');
        const emptyEl = document.getElementById('history-empty');
        if (listEl) listEl.innerHTML = '';
        if (emptyEl) emptyEl.style.display = 'none';
        loadOrders(0);
        setupInfiniteScroll();
    }

    async function loadOrders(page) {
        if (isLoading || !hasMore) return;
        isLoading = true;
        const loadingEl = document.getElementById('history-loading');
        if (loadingEl) loadingEl.style.display = 'block';
        try {
            const storeId = Auth.getStoreId();
            const data = await Api.get(`/api/stores/${storeId}/customer/orders?page=${page}&size=10`);
            const listEl = document.getElementById('order-history-list');
            const emptyEl = document.getElementById('history-empty');
            if (data.orders.length === 0 && page === 0) {
                if (emptyEl) emptyEl.style.display = 'block';
            } else {
                data.orders.forEach(order => { if (listEl) listEl.appendChild(renderOrderCard(order)); });
            }
            currentPage = page;
            hasMore = page < data.totalPages - 1;
        } catch (e) {
            App.showToast(e.message || '주문 내역을 불러올 수 없습니다', 'error');
        } finally {
            isLoading = false;
            const loadingEl = document.getElementById('history-loading');
            if (loadingEl) loadingEl.style.display = 'none';
        }
    }

    function setupInfiniteScroll() {
        if (observer) observer.disconnect();
        const sentinel = document.getElementById('history-sentinel');
        if (!sentinel) return;
        observer = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting && hasMore && !isLoading) { loadOrders(currentPage + 1); }
        });
        observer.observe(sentinel);
    }

    function renderOrderCard(order) {
        const card = document.createElement('div');
        card.className = 'card order-history-card';
        const badge = getStatusBadge(order.status);
        const time = new Date(order.createdAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
        card.innerHTML = `
            <div class="card-header d-flex justify-content-between align-items-center" data-bs-toggle="collapse" data-bs-target="#order-detail-${order.orderId}">
                <div>
                    <strong>#${App.escapeHtml(order.orderNumber)}</strong>
                    <small class="text-muted ms-2">${time}</small>
                </div>
                <div>
                    <span class="badge ${badge.cssClass} me-2">${badge.text}</span>
                    <strong>${App.formatPrice(order.totalAmount)}원</strong>
                </div>
            </div>
            <div class="collapse" id="order-detail-${order.orderId}">
                <div class="card-body p-2">
                    ${order.items.map(item => `
                        <div class="d-flex justify-content-between py-1 border-bottom">
                            <div>
                                <span>${App.escapeHtml(item.menuName)}</span>
                                <small class="text-muted"> x${item.quantity}</small>
                                ${item.spicyOption ? `<span class="badge bg-danger bg-opacity-25 text-danger ms-1">${App.escapeHtml(item.spicyOption)}</span>` : ''}
                                ${item.specialRequest ? `<br><small class="text-muted"><i class="bi bi-chat-dots"></i> ${App.escapeHtml(item.specialRequest)}</small>` : ''}
                            </div>
                            <span>${App.formatPrice(item.unitPrice * item.quantity)}원</span>
                        </div>
                    `).join('')}
                </div>
            </div>`;
        return card;
    }

    function getStatusBadge(status) {
        const map = {
            'WAITING': { text: '대기중', cssClass: 'bg-warning' },
            'PREPARING': { text: '준비중', cssClass: 'bg-primary' },
            'DONE': { text: '완료', cssClass: 'bg-success' }
        };
        return map[status] || { text: status, cssClass: 'bg-secondary' };
    }

    return { render, loadOrders, renderOrderCard, getStatusBadge };
})();
