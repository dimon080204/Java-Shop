package com.example.Shop.services;

import com.example.Shop.dto.PurchaseDTO;
import com.example.Shop.entities.Product;
import com.example.Shop.entities.Purchase;
import com.example.Shop.entities.Staff;
import com.example.Shop.repositories.ProductRepository;
import com.example.Shop.repositories.PurchaseRepository;
import com.example.Shop.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StaffRepository staffRepository;

    public List<PurchaseDTO> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PurchaseDTO getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<PurchaseDTO> getPurchasesByProductName(String name) {
        return purchaseRepository.findByProductNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PurchaseDTO> getPurchasesByStaffLastName(String lastName) {
        return purchaseRepository.findByStaffLastNameContainingIgnoreCase(lastName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PurchaseDTO> getPurchasesByDateRange(LocalDateTime start, LocalDateTime end) {
        return purchaseRepository.findByPurchaseDateBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseDTO updatePurchase(Long id, PurchaseDTO dto) {
        Purchase existing = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        Product product = existing.getProduct();

        // 1. Temporary cancel the old purchase quantity from the product stock
        int quantityWithoutOldPurchase = product.getQuantity() - existing.getQuantity();

        // Negative quantity check
        if (quantityWithoutOldPurchase < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot update purchase: items from this batch are already sold. Current stock: " + product.getQuantity());
        }

        product.setQuantity(quantityWithoutOldPurchase);

        // 2. Update data from DTO
        existing.setQuantity(dto.getQuantity());
        existing.setPrice(dto.getPrice());

        if (dto.getQuantity() != null && dto.getPrice() != null) {
            existing.setCost(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        }

        // 3. Add new purchase quantity to the product stock
        product.setQuantity(product.getQuantity() + dto.getQuantity());

        productRepository.save(product);
        return convertToDTO(purchaseRepository.save(existing));
    }

    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        // Remove the quantity of the product from the stock when deleting a purchase
        Product product = purchase.getProduct();
        if (product.getQuantity() < purchase.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete purchase: product already sold. Stock would go negative!");
        }
        product.setQuantity(product.getQuantity() - purchase.getQuantity());
        productRepository.save(product);

        purchaseRepository.delete(purchase);
    }

    @Transactional // Important! This ensures that all operations within this method are atomic
    public PurchaseDTO createPurchase(PurchaseDTO dto) {
        // 1. Search for the product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 2. Search for the staff member
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        // 3. Create a new purchase entity and set its fields
        Purchase purchase = new Purchase();
        purchase.setProduct(product);
        purchase.setStaff(staff);
        purchase.setQuantity(dto.getQuantity());
        purchase.setPrice(dto.getPrice());

        // Calculate cost (quantity * price) if both are provided
        if (dto.getQuantity() != null && dto.getPrice() != null) {
            purchase.setCost(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        }

        // 4. Update the product quantity (add the purchased quantity to the existing quantity)
        product.setQuantity(product.getQuantity() + dto.getQuantity());
        productRepository.save(product);

        // 5. Save the purchase and return the DTO
        return convertToDTO(purchaseRepository.save(purchase));
    }

    private PurchaseDTO convertToDTO(Purchase purchase) {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setId(purchase.getId());
        dto.setQuantity(purchase.getQuantity());
        dto.setPrice(purchase.getPrice());
        dto.setCost(purchase.getCost());
        dto.setPurchaseDate(purchase.getPurchaseDate());
        dto.setProductId(purchase.getProduct().getId());
        dto.setStaffId(purchase.getStaff().getId());

        if (purchase.getProduct() != null) {
            dto.setProductName(purchase.getProduct().getName());
        }
        if (purchase.getStaff() != null) {
            dto.setStaffLastName(purchase.getStaff().getLastName());
        }

        return dto;
    }
}
