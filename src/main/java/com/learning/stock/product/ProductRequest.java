package com.learning.stock.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom du produit ne peut pas dépasser 100 caractères")
    private String name;

    @NotBlank(message = "Le SKU du produit est obligatoire")
    @Size(max = 50, message = "Le SKU du produit ne peut pas dépasser 50 caractères")
    private String sku;

    @Size(max = 500, message = "La description du produit ne peut pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "Le prix du produit est obligatoire")
    @DecimalMin(value = "0.00", inclusive = false, message = "Le prix du produit doit être supérieur à 0")
    private BigDecimal price;

    @NotNull(message = "La quantité en stock est obligatoire")
    @Min(value = 0, message = "La quantité en stock ne peut pas être négative")
    private Integer quantity;

    public ProductRequest() {
    }

    public ProductRequest(String name, String sku, String description, BigDecimal price, Integer quantity) {
        this.name = name;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    // Setters (nécessaires pour le binding de la requête)
    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
