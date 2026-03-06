/**
 * 장바구니 모듈 - localStorage 기반 CRUD, 총 금액/수량 계산
 */
const Cart = (function () {
    const STORAGE_KEY = 'cart_items';

    function getItems() {
        const data = localStorage.getItem(STORAGE_KEY);
        if (!data) return [];
        try { return JSON.parse(data); } catch (e) { return []; }
    }

    function _saveItems(items) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    }

    function addItem(menu) {
        const items = getItems();
        const existing = items.find(item => item.menuId === menu.id);
        if (existing) {
            existing.quantity += 1;
        } else {
            items.push({
                menuId: menu.id, menuName: menu.name, unitPrice: menu.price,
                quantity: 1, hasSpicyOptions: menu.hasSpicyOptions || false,
                spicyOption: null, specialRequest: null
            });
        }
        _saveItems(items);
    }

    function updateQuantity(menuId, delta) {
        const items = getItems();
        const idx = items.findIndex(item => item.menuId === menuId);
        if (idx === -1) return;
        items[idx].quantity += delta;
        if (items[idx].quantity <= 0) { items.splice(idx, 1); }
        _saveItems(items);
    }

    function removeItem(menuId) {
        const items = getItems().filter(item => item.menuId !== menuId);
        _saveItems(items);
    }

    function clear() { localStorage.removeItem(STORAGE_KEY); }

    function getTotalAmount() {
        return getItems().reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
    }

    function getTotalCount() {
        return getItems().reduce((sum, item) => sum + item.quantity, 0);
    }

    function getItemQuantity(menuId) {
        const item = getItems().find(i => i.menuId === menuId);
        return item ? item.quantity : 0;
    }

    return { STORAGE_KEY, getItems, addItem, updateQuantity, removeItem, clear, getTotalAmount, getTotalCount, getItemQuantity };
})();
