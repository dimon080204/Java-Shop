package com.example.Shop.repositories;

import com.example.Shop.entities.Purchase;
import com.example.Shop.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByProductNameContainingIgnoreCase(String productName);
    List<Purchase> findByStaffLastNameContainingIgnoreCase(String lastName);
    List<Purchase> findByPurchaseDateBetween(LocalDateTime start, LocalDateTime end);
}
