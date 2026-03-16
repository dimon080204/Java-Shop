package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String sku;
    private String name;
    private int quantity;
    private BigDecimal price;
    private String category;
    private Map<String, Object> description;
}
