package com.tableorder.customer.dto;

import java.util.List;

public class SpicyOptionsResponse {
    private List<SpicyOptionDto> options;

    public SpicyOptionsResponse() {}
    public SpicyOptionsResponse(List<SpicyOptionDto> options) { this.options = options; }
    public List<SpicyOptionDto> getOptions() { return options; }
    public void setOptions(List<SpicyOptionDto> options) { this.options = options; }
}
