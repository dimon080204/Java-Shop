package com.example.Shop.controllers;

import com.example.Shop.dto.PurchaseDTO;
import com.example.Shop.services.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping
    public PurchaseDTO create(@RequestBody PurchaseDTO dto) {
        return purchaseService.createPurchase(dto);
    }

    @GetMapping
    public List<PurchaseDTO> getAll() {
        return purchaseService.getAllPurchases();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseDTO> getById(@PathVariable Long id) {
        PurchaseDTO dto = purchaseService.getPurchaseById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search/product")
    public List<PurchaseDTO> getByProductName(@RequestParam String productName) {
        return purchaseService.getPurchasesByProductName(productName);
    }

    @GetMapping("/search/staff")
    public List<PurchaseDTO> getByStaffLastName(@RequestParam String staffLastName) {
        return purchaseService.getPurchasesByStaffLastName(staffLastName);
    }

    // Searh by date range: /api/purchases/search/dates?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59
    @GetMapping("/search/dates")
    public List<PurchaseDTO> getByDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return purchaseService.getPurchasesByDateRange(start, end);
    }

    @PutMapping("/{id}")
    public PurchaseDTO update(@PathVariable Long id, @RequestBody PurchaseDTO dto) {
        return purchaseService.updatePurchase(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
    }
}