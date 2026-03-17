package com.example.Shop.services;

import com.example.Shop.dto.PurchaseDTO;
import com.example.Shop.dto.SaleDTO;
import com.example.Shop.entities.Product;
import com.example.Shop.entities.Purchase;
import com.example.Shop.entities.Sale;
import com.example.Shop.entities.Staff;
import com.example.Shop.repositories.ProductRepository;
import com.example.Shop.repositories.SaleRepository;
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
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StaffRepository staffRepository;

    public List<SaleDTO> getAllSales() {
        return saleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SaleDTO getSaleById(Long id) {
        return saleRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<SaleDTO> getSalesByProductName(String name) {
        return saleRepository.findByProductNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SaleDTO> getSalesByStaffLastName(String lastName) {
        return saleRepository.findByStaffLastNameContainingIgnoreCase(lastName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SaleDTO> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));

        // Remove updated quantity of the product from the stock when deleting a sale
        Product product = sale.getProduct();
        product.setQuantity(product.getQuantity() + sale.getQuantity());
        productRepository.save(product);

        saleRepository.delete(sale);
    }

    @Transactional
    public SaleDTO updateSale(Long id, SaleDTO dto) {
        if (dto.getDiscount() == null) dto.setDiscount(BigDecimal.ZERO);
        Sale existing = saleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));

        Product product = existing.getProduct();

        // 1. Temporary cancel the old sale quantity from the product stock
        int quantityWithoutOldSale = product.getQuantity() + existing.getQuantity();
        product.setQuantity(quantityWithoutOldSale);

        // 2. Update data from DTO
        existing.setQuantity(dto.getQuantity());
        existing.setPrice(dto.getPrice());
        existing.setDiscount(dto.getDiscount());

        if (dto.getQuantity() != null && dto.getPrice() != null) {
            // Calculate total price with discount
            BigDecimal totalPrice = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            if (dto.getDiscount() != null) {
                totalPrice = totalPrice.subtract(dto.getDiscount());
            }
            existing.setTotalPrice(totalPrice);
        }

        // 3. Remove new sale quantity to the product stock
        int quantityWithNewSale = product.getQuantity() - dto.getQuantity();
        // Negative quantity check
        if (quantityWithNewSale < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough stock for updated sale. Available: " + product.getQuantity());
        }
        product.setQuantity(quantityWithNewSale);

        existing.setPaymentMethod(dto.getPaymentMethod());

        productRepository.save(product);
        return convertToDTO(saleRepository.save(existing));
    }

    @Transactional // Important! This ensures that all operations within this method are atomic
    public SaleDTO createSale(SaleDTO dto) {
        if (dto.getDiscount() == null) dto.setDiscount(BigDecimal.ZERO);
        // 1. Search for the product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 2. Search for the staff member
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff member not found"));

        // 3. Create a new sale entity and set its fields
        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setStaff(staff);
        sale.setQuantity(dto.getQuantity());
        sale.setPrice(dto.getPrice());
        sale.setDiscount(dto.getDiscount());
        sale.setPaymentMethod(dto.getPaymentMethod());

        // Calculate cost (quantity * price - discount) if both are provided
        if (dto.getQuantity() != null && dto.getPrice() != null) {
            BigDecimal totalPrice = dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            if (dto.getDiscount() != null) {
                totalPrice = totalPrice.subtract(dto.getDiscount());
            }
            sale.setTotalPrice(totalPrice);
        }

        // 4. Update the product quantity (remove the sold quantity to the existing quantity)
        int newProductQuantity = product.getQuantity() - dto.getQuantity();
        // Negative quantity check
        if (newProductQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough stock for updated sale. Available: " + product.getQuantity());
        }
        product.setQuantity(newProductQuantity);
        productRepository.save(product);

        // 5. Save the sale and return the DTO
        return convertToDTO(saleRepository.save(sale));
    }

    private SaleDTO convertToDTO(com.example.Shop.entities.Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setQuantity(sale.getQuantity());
        dto.setPrice(sale.getPrice());
        dto.setDiscount(sale.getDiscount());
        dto.setTotalPrice(sale.getTotalPrice());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setSaleDate(sale.getSaleDate());
        dto.setProductId(sale.getProduct().getId());
        dto.setStaffId(sale.getStaff().getId());

        if (sale.getProduct() != null) {
            dto.setProductName(sale.getProduct().getName());
        }
        if (sale.getStaff() != null) {
            dto.setStaffLastName(sale.getStaff().getLastName());
        }

        return dto;
    }
}
