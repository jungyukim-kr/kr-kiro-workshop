package com.tableorder.customer.dto;

public class MenuDto {
    private Long id;
    private String name;
    private int price;
    private String description;
    private String category;
    private String imageUrl;
    private String spicyLevel;
    private boolean hasSpicyOptions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSpicyLevel() { return spicyLevel; }
    public void setSpicyLevel(String spicyLevel) { this.spicyLevel = spicyLevel; }
    public boolean isHasSpicyOptions() { return hasSpicyOptions; }
    public void setHasSpicyOptions(boolean hasSpicyOptions) { this.hasSpicyOptions = hasSpicyOptions; }
}
