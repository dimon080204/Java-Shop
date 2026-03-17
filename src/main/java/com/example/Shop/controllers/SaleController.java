package com.example.Shop.controllers;

import com.example.Shop.dto.PurchaseDTO;
import com.example.Shop.dto.SaleDTO;
import com.example.Shop.services.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @PostMapping
    public SaleDTO create(@RequestBody SaleDTO dto) {
        return saleService.createSale(dto);
    }

    @GetMapping
    public List<SaleDTO> getAll() {
        return saleService.getAllSales();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getById(@PathVariable Long id) {
        SaleDTO dto = saleService.getSaleById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search/product")
    public List<SaleDTO> getByProduct(@RequestParam String name) {
        return saleService.getSalesByProductName(name);
    }

    @GetMapping("/search/staff")
    public List<SaleDTO> getByStaffLastName(@RequestParam String staffLastName) {
        return saleService.getSalesByStaffLastName(staffLastName);
    }

    @GetMapping("/search/dates")
    public List<SaleDTO> getByDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return saleService.getSalesByDateRange(start, end);
    }

    @PutMapping("/{id}")
    public SaleDTO update(@PathVariable Long id, @RequestBody SaleDTO dto) {
        return saleService.updateSale(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        saleService.deleteSale(id);
    }
}
