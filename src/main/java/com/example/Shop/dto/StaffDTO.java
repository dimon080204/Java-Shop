package com.example.Shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StaffDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
}
