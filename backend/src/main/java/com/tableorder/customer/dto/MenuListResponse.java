package com.tableorder.customer.dto;

import java.util.List;

public class MenuListResponse {
    private List<MenuDto> menus;

    public MenuListResponse() {}
    public MenuListResponse(List<MenuDto> menus) { this.menus = menus; }
    public List<MenuDto> getMenus() { return menus; }
    public void setMenus(List<MenuDto> menus) { this.menus = menus; }
}
