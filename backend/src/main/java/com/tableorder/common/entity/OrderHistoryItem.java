package com.tableorder.common.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_history_item")
public class OrderHistoryItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_history_id", nullable = false)
    private Long orderHistoryId;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "spicy_option", length = 50)
    private String spicyOption;

    @Column(name = "special_request")
    private String specialRequest;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderHistoryId() { return orderHistoryId; }
    public void setOrderHistoryId(Long orderHistoryId) { this.orderHistoryId = orderHistoryId; }
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
