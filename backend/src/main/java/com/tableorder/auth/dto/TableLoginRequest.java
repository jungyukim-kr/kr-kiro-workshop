package com.tableorder.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TableLoginRequest {
    @NotBlank(message = "매장 식별자를 입력해주세요")
    @Size(max = 50)
    private String storeCode;

    @NotNull(message = "테이블 번호를 입력해주세요")
    @Positive(message = "테이블 번호를 입력해주세요")
    private Integer tableNumber;

    @NotBlank(message = "4자리 숫자 PIN을 입력해주세요")
    @Pattern(regexp = "^\\d{4}$", message = "4자리 숫자 PIN을 입력해주세요")
    private String password;

    public TableLoginRequest() {}
    public TableLoginRequest(String storeCode, Integer tableNumber, String password) {
        this.storeCode = storeCode;
        this.tableNumber = tableNumber;
        this.password = password;
    }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
