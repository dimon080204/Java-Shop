package com.example.Shop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExternalProductDTO {
    private String title;
    private String description;
    private String sku;
    private BigDecimal price;
    private String category;
    private String thumbnail; // Image link might be useful for external products
}