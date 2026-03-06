package com.tableorder.customer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private int quantity;

    @Min(value = 1, message = "단가는 1 이상이어야 합니다")
    private int unitPrice;

    private String spicyOption;
    private String specialRequest;

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public String getSpicyOption() { return spicyOption; }
    public void setSpicyOption(String spicyOption) { this.spicyOption = spicyOption; }
    public String getSpecialRequest() { return specialRequest; }
    public void setSpecialRequest(String specialRequest) { this.specialRequest = specialRequest; }
}
