package com.example.Shop.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AnalyticsDTO {
    private Long productId;
    private String productName;
    private BigDecimal profit;
    private String abcCategory; // A, B, or C
    private Double share;       // Percent of total profit
}