package com.example.Shop.dto;

import lombok.Data;
import java.util.List;

@Data
public class DummyJsonResponse {
    // Name must be "products", like in JSON from API
    private List<ExternalProductDTO> products;
    private Integer total;
    private Integer skip;
    private Integer limit;
}
