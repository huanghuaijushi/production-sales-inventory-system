package com.hhjs.psi.product.dto;

public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String type;
    private Long categoryId;
    private String category;
    private String unit;
    private String specification;
    private Double costPrice;
    private Integer alertQuantity;
    private String description;

    public ProductResponse() {}

    public ProductResponse(
            Long id,
            String sku,
            String name,
            String type,
            Long categoryId,
            String category,
            String unit,
            String specification,
            Double costPrice,
            Integer alertQuantity,
            String description
    ) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.type = type;
        this.categoryId = categoryId;
        this.category = category;
        this.unit = unit;
        this.specification = specification;
        this.costPrice = costPrice;
        this.alertQuantity = alertQuantity;
        this.description = description;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }

    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }

    public Integer getAlertQuantity() { return alertQuantity; }
    public void setAlertQuantity(Integer alertQuantity) { this.alertQuantity = alertQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
