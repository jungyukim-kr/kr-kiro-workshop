/**
 * 주문 확인/생성 모듈 - 맵기 옵션, 요청사항, 주문 전송
 */
const OrderView = (function () {
    let spicyOptionsCache = {};

    function render() {
        spicyOptionsCache = {};
        const items = Cart.getItems();
        const listEl = document.getElementById('order-items-list');
        const totalEl = document.getElementById('order-total-amount');
        const submitBtn = document.getElementById('btn-submit-order');

        if (!listEl) return;
        listEl.innerHTML = '';

        if (items.length === 0) {
            listEl.innerHTML = '<div class="text-center text-muted py-5"><p>장바구니가 비어있습니다</p></div>';
            if (submitBtn) submitBtn.disabled = true;
            if (totalEl) totalEl.textContent = '0';
            return;
        }

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.onclick = submitOrder;
        }

        items.forEach((item, idx) => {
            const card = document.createElement('div');
            card.className = 'order-item-card';
            card.innerHTML = `
                <div class="d-flex justify-content-between mb-2">
                    <strong>${App.escapeHtml(item.menuName)}</strong>
                    <span>${item.quantity} x ${App.formatPrice(item.unitPrice)}원 = <strong>${App.formatPrice(item.unitPrice * item.quantity)}원</strong></span>
                </div>
                ${item.hasSpicyOptions ? `
                    <div class="mb-2">
                        <label class="form-label small">맵기 옵션</label>
                        <select class="form-select form-select-sm" id="spicy-select-${idx}" data-menu-id="${item.menuId}">
                            <option value="">선택 안 함</option>
                        </select>
                    </div>
                ` : ''}
                <div class="mb-0">
                    <label class="form-label small">요청사항</label>
                    <textarea class="form-control form-control-sm" id="request-input-${idx}" rows="1" placeholder="예: 청양고추 빼주세요"></textarea>
                </div>`;
            listEl.appendChild(card);

            if (item.hasSpicyOptions) {
                loadSpicyOptions(item.menuId).then(options => {
                    const select = document.getElementById(`spicy-select-${idx}`);
                    if (select && options) {
                        options.forEach(opt => {
                            const optEl = document.createElement('option');
                            optEl.value = opt.optionName;
                            optEl.textContent = opt.optionName;
                            select.appendChild(optEl);
                        });
                    }
                });
            }
        });

        if (totalEl) totalEl.textContent = App.formatPrice(Cart.getTotalAmount());
    }

    async function loadSpicyOptions(menuId) {
        if (spicyOptionsCache[menuId]) return spicyOptionsCache[menuId];
        try {
            const storeId = Auth.getStoreId();
            const data = await Api.get(`/api/stores/${storeId}/customer/menus/${menuId}/spicy-options`);
            spicyOptionsCache[menuId] = data.options;
            return data.options;
        } catch (e) {
            return [];
        }
    }

    async function submitOrder() {
        const submitBtn = document.getElementById('btn-submit-order');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 주문 중...';
        }

        try {
            const items = Cart.getItems();
            const orderItems = items.map((item, idx) => {
                const spicySelect = document.getElementById(`spicy-select-${idx}`);
                const requestInput = document.getElementById(`request-input-${idx}`);
                return {
                    menuId: item.menuId,
                    quantity: item.quantity,
                    unitPrice: item.unitPrice,
                    spicyOption: spicySelect ? (spicySelect.value || null) : null,
                    specialRequest: requestInput ? (requestInput.value.trim() || null) : null
                };
            });

            const storeId = Auth.getStoreId();
            const response = await Api.post(`/api/stores/${storeId}/customer/orders`, { items: orderItems });

            // 성공 처리
            Cart.clear();
            if (response.sessionId) {
                Auth.updateSessionId(response.sessionId);
            }
            App.showToast(`주문 완료! 주문번호: ${response.orderNumber}`, 'success');
            MenuView.refreshCartBadges();

            // 5초 후 메뉴 화면 이동
            setTimeout(() => {
                window.location.hash = '#/menu';
            }, 5000);

        } catch (e) {
            App.showToast(e.message || '주문에 실패했습니다', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '주문 확정';
            }
        }
    }

    return { render, loadSpicyOptions, submitOrder };
})();
