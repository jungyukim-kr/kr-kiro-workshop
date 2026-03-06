package com.tableorder.customer.dto;

public class SpicyOptionDto {
    private Long id;
    private String optionName;

    public SpicyOptionDto() {}
    public SpicyOptionDto(Long id, String optionName) { this.id = id; this.optionName = optionName; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }
}
