package com.hhjs.psi.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequest {

    @NotBlank(message = "产品编码不能为空")
    @Size(max = 64, message = "产品编码长度不能超过64个字符")
    private String code;

    @NotBlank(message = "产品名称不能为空")
    @Size(max = 120, message = "产品名称长度不能超过120个字符")
    private String name;

    @NotNull(message = "产品类型不能为空")
    private String type;

    @Size(max = 64, message = "产品类别长度不能超过64个字符")
    private String category;

    @Size(max = 64, message = "规格型号长度不能超过64个字符")
    private String specification;

    @NotBlank(message = "计量单位不能为空")
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    private String unit;

    private Double costPrice;

    private Double salePrice;

    private Integer alertQuantity;

    private String description;

    // Constructors
    public ProductRequest() {}

    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }

    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }

    public Integer getAlertQuantity() { return alertQuantity; }
    public void setAlertQuantity(Integer alertQuantity) { this.alertQuantity = alertQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}