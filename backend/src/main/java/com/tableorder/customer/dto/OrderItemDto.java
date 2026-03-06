package com.tableorder.customer.dto;

public class OrderItemDto {
    private String menuName;
    private int quantity;
    private int unitPrice;
    private String spicyOption;
    private String specialRequest;

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public String getSpicyOption() { return spicyOption; }
    public void setSpicyOption(String spicyOption) { this.spicyOption = spicyOption; }
    public String getSpecialRequest() { return specialRequest; }
    public void setSpecialRequest(String specialRequest) { this.specialRequest = specialRequest; }
}
