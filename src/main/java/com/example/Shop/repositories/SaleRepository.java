package com.example.Shop.repositories;

import com.example.Shop.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByProductNameContainingIgnoreCase(String productName);
    List<Sale> findByStaffLastNameContainingIgnoreCase(String lastName);
    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
}
