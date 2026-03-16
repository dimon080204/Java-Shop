package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    // For POST request
    private Long productId;
    // For GET request
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String productName;

    private Long staffId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String staffLastName;

    private Integer quantity;
    private BigDecimal price;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal cost; // Calculated as price * quantity, read-only
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime purchaseDate;
}
