/**
 * 메뉴 화면 모듈 - 카테고리/메뉴 조회, 카드 렌더링
 */
const MenuView = (function () {
    let currentCategory = null;
    let menus = [];

    function render() {
        loadCategories();
    }

    async function loadCategories() {
        try {
            const storeId = Auth.getStoreId();
            const data = await Api.get(`/api/stores/${storeId}/customer/categories`);
            const tabsEl = document.getElementById('category-tabs');
            if (!tabsEl) return;
            tabsEl.innerHTML = '';

            data.categories.forEach((cat, idx) => {
                const btn = document.createElement('button');
                btn.className = 'nav-link' + (idx === 0 ? ' active' : '');
                btn.textContent = cat;
                btn.addEventListener('click', () => {
                    tabsEl.querySelectorAll('.nav-link').forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                    loadMenus(cat);
                });
                tabsEl.appendChild(btn);
            });

            if (data.categories.length > 0) {
                loadMenus(data.categories[0]);
            }
        } catch (e) {
            App.showToast(e.message || '카테고리를 불러올 수 없습니다', 'error');
        }
    }

    async function loadMenus(category) {
        currentCategory = category;
        try {
            const storeId = Auth.getStoreId();
            const data = await Api.get(`/api/stores/${storeId}/customer/menus?category=${encodeURIComponent(category)}`);
            menus = data.menus;
            const gridEl = document.getElementById('menu-grid');
            if (!gridEl) return;
            gridEl.innerHTML = '';

            menus.forEach(menu => {
                const col = document.createElement('div');
                col.className = 'col-6 col-sm-4 col-md-3';
                col.appendChild(renderMenuCard(menu));
                gridEl.appendChild(col);
            });
        } catch (e) {
            App.showToast(e.message || '메뉴를 불러올 수 없습니다', 'error');
        }
    }

    function renderMenuCard(menu) {
        const card = document.createElement('div');
        card.className = 'card menu-card h-100';

        const qty = Cart.getItemQuantity(menu.id);
        const imgHtml = menu.imageUrl
            ? `<img src="${App.escapeHtml(menu.imageUrl)}" class="card-img-top" alt="${App.escapeHtml(menu.name)}">`
            : `<div class="card-img-placeholder"><i class="bi bi-egg-fried"></i></div>`;

        const spicyBadge = menu.spicyLevel
            ? `<span class="badge bg-danger bg-opacity-25 text-danger">${App.escapeHtml(menu.spicyLevel)}</span>`
            : '';

        card.innerHTML = `
            ${imgHtml}
            <div class="card-body p-2">
                <h6 class="card-title mb-1">${App.escapeHtml(menu.name)} ${spicyBadge}</h6>
                <p class="card-text text-muted small mb-1">${App.escapeHtml(menu.description || '')}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <strong>${App.formatPrice(menu.price)}원</strong>
                    <div class="qty-controls" id="qty-ctrl-${menu.id}">
                        ${qty > 0 ? `
                            <button class="btn btn-outline-secondary btn-sm" onclick="MenuView._changeQty(${menu.id}, -1)">-</button>
                            <span class="fw-bold">${qty}</span>
                            <button class="btn btn-primary btn-sm" onclick="MenuView._changeQty(${menu.id}, 1)">+</button>
                        ` : `
                            <button class="btn btn-primary btn-sm" onclick="MenuView._addToCart(${menu.id})">+</button>
                        `}
                    </div>
                </div>
            </div>`;
        return card;
    }

    function _addToCart(menuId) {
        const menu = menus.find(m => m.id === menuId);
        if (!menu) return;
        Cart.addItem(menu);
        refreshCartBadges();
        if (currentCategory) loadMenus(currentCategory);
    }

    function _changeQty(menuId, delta) {
        Cart.updateQuantity(menuId, delta);
        refreshCartBadges();
        if (currentCategory) loadMenus(currentCategory);
    }

    function refreshCartBadges() {
        const count = Cart.getTotalCount();
        const badge = document.getElementById('cart-badge');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline' : 'none';
        }
    }

    return { render, loadCategories, loadMenus, renderMenuCard, refreshCartBadges, _addToCart, _changeQty };
})();
