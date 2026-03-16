package com.example.Shop.controllers;

import com.example.Shop.dto.ExternalProductDTO;
import com.example.Shop.services.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/import")
public class ImportController {

    @Autowired
    private ImportService importService;

    @GetMapping("/search-external")
    public List<ExternalProductDTO> search(@RequestParam String query) {
        return importService.searchExternalProducts(query);
    }
}
