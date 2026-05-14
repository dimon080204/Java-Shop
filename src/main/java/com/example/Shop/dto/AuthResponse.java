package com.example.Shop.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor // Creates a constructor with all fields as parameters
public class AuthResponse {
    private String token;
}