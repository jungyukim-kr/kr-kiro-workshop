/**
 * 앱 초기화 + Hash Router + 공통 UI (Toast, Spinner) + Cart Panel
 */
const App = (function () {
    const views = ['login', 'menu', 'order', 'history'];

    function init() {
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                const storeCode = document.getElementById('input-store-code').value.trim();
                const tableNumber = Number(document.getElementById('input-table-number').value);
                const pin = document.getElementById('input-pin').value.trim();
                const errorEl = document.getElementById('login-error');
                if (!storeCode || !tableNumber || !pin) {
                    if (errorEl) { errorEl.textContent = '모든 항목을 입력해주세요'; errorEl.classList.remove('d-none'); }
                    return;
                }
                if (!/^\d{4}$/.test(pin)) {
                    if (errorEl) { errorEl.textContent = '4자리 숫자 PIN을 입력해주세요'; errorEl.classList.remove('d-none'); }
                    return;
                }
                try {
                    if (errorEl) errorEl.classList.add('d-none');
                    await Auth.login(storeCode, tableNumber, pin);
                    updateHeaderInfo();
                    window.location.hash = '#/menu';
                } catch (e) {
                    if (errorEl) { errorEl.textContent = e.message || '로그인에 실패했습니다'; errorEl.classList.remove('d-none'); }
                }
            });
        }
        const cartOpenBtn = document.getElementById('btn-cart-open');
        if (cartOpenBtn) {
            cartOpenBtn.addEventListener('click', function () {
                renderCartPanel();
                const offcanvas = new bootstrap.Offcanvas(document.getElementById('cart-offcanvas'));
                offcanvas.show();
            });
        }
        const cartClearBtn = document.getElementById('btn-cart-clear');
        if (cartClearBtn) {
            cartClearBtn.addEventListener('click', function () {
                Cart.clear(); renderCartPanel(); MenuView.refreshCartBadges();
            });
        }
        window.addEventListener('hashchange', function () { navigate(window.location.hash); });
        autoLogin();
    }

    async function autoLogin() {
        showSpinner();
        try {
            const success = await Auth.init();
            if (success) {
                updateHeaderInfo();
                if (!window.location.hash || window.location.hash === '#/' || window.location.hash === '#/login') {
                    window.location.hash = '#/menu';
                } else { navigate(window.location.hash); }
            } else { window.location.hash = '#/login'; }
        } catch (e) { window.location.hash = '#/login'; }
        finally { hideSpinner(); }
    }

    function navigate(hash) {
        const route = hash.replace('#/', '') || 'menu';
        if (route !== 'login' && !Auth.isLoggedIn()) { window.location.hash = '#/login'; return; }
        showView(route);
        switch (route) {
            case 'menu': MenuView.render(); MenuView.refreshCartBadges(); break;
            case 'order': OrderView.render(); break;
            case 'history': OrderHistoryView.render(); break;
            case 'login': break;
        }
    }

    function showView(viewName) {
        views.forEach(v => {
            const el = document.getElementById('view-' + v);
            if (el) el.style.display = (v === viewName) ? 'block' : 'none';
        });
        const header = document.getElementById('main-header');
        if (header) header.style.display = (viewName === 'login') ? 'none' : 'flex';
        document.querySelectorAll('#main-header .nav-link').forEach(link => link.classList.remove('active'));
        const activeNav = document.getElementById('nav-' + viewName);
        if (activeNav) activeNav.classList.add('active');
    }

    function updateHeaderInfo() {
        const tableNum = localStorage.getItem('auth_tableNumber');
        const tableEl = document.getElementById('header-table-number');
        if (tableEl && tableNum) tableEl.textContent = tableNum;
    }

    function renderCartPanel() {
        const items = Cart.getItems();
        const listEl = document.getElementById('cart-items-list');
        const emptyEl = document.getElementById('cart-empty');
        const footerEl = document.getElementById('cart-footer');
        const totalEl = document.getElementById('cart-total-amount');
        if (!listEl) return;
        listEl.innerHTML = '';
        if (items.length === 0) {
            if (emptyEl) emptyEl.style.display = 'block';
            if (footerEl) footerEl.style.display = 'none';
            return;
        }
        if (emptyEl) emptyEl.style.display = 'none';
        if (footerEl) footerEl.style.display = 'block';
        items.forEach(item => {
            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <div>
                    <div class="fw-bold">${escapeHtml(item.menuName)}</div>
                    <small class="text-muted">${formatPrice(item.unitPrice)}원</small>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <div class="qty-controls">
                        <button class="btn btn-outline-secondary btn-sm" onclick="App._cartChangeQty(${item.menuId}, -1)">-</button>
                        <span class="fw-bold">${item.quantity}</span>
                        <button class="btn btn-primary btn-sm" onclick="App._cartChangeQty(${item.menuId}, 1)">+</button>
                    </div>
                    <button class="btn btn-outline-danger btn-sm" onclick="App._cartRemove(${item.menuId})"><i class="bi bi-x"></i></button>
                </div>`;
            listEl.appendChild(div);
        });
        if (totalEl) totalEl.textContent = formatPrice(Cart.getTotalAmount());
    }

    function _cartChangeQty(menuId, delta) { Cart.updateQuantity(menuId, delta); renderCartPanel(); MenuView.refreshCartBadges(); }
    function _cartRemove(menuId) { Cart.removeItem(menuId); renderCartPanel(); MenuView.refreshCartBadges(); }

    function showToast(message, type) {
        const toastEl = document.getElementById('app-toast');
        const msgEl = document.getElementById('toast-message');
        if (!toastEl || !msgEl) return;
        msgEl.textContent = message;
        toastEl.className = 'toast';
        if (type === 'error') toastEl.classList.add('bg-danger', 'text-white');
        else if (type === 'success') toastEl.classList.add('bg-success', 'text-white');
        else toastEl.classList.add('bg-dark', 'text-white');
        const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
        toast.show();
    }

    function showSpinner() { const el = document.getElementById('app-spinner'); if (el) el.style.display = 'flex'; }
    function hideSpinner() { const el = document.getElementById('app-spinner'); if (el) el.style.display = 'none'; }

    function escapeHtml(str) {
        if (!str) return '';
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(str).replace(/[&<>"']/g, c => map[c]);
    }

    function formatPrice(price) { return Number(price).toLocaleString('ko-KR'); }

    return { init, navigate, showView, showToast, showSpinner, hideSpinner, escapeHtml, formatPrice, _cartChangeQty, _cartRemove };
})();

document.addEventListener('DOMContentLoaded', function () { App.init(); });
