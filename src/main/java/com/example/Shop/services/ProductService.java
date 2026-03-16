package com.example.Shop.services;

import com.example.Shop.dto.ProductDTO;
import com.example.Shop.entities.Product;
import com.example.Shop.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        // Get all goods from repository and transform them into DTO list
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public ProductDTO createProduct(Product product) {
        productRepository.findBySku(product.getSku()).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU " + p.getSku() + " already exists for product with ID " + p.getId());
        });

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long id, Product productDetails) {
        // 1. Check if the product exists by ID
        return productRepository.findById(id).map(existingProduct -> {
            // 2. Updating fields of the existing product with new details
            existingProduct.setSku(productDetails.getSku());
            existingProduct.setName(productDetails.getName());
            existingProduct.setQuantity(productDetails.getQuantity());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setCategory(productDetails.getCategory());
            existingProduct.setDescription(productDetails.getDescription());

            // 3. Saving the updated product back to the repository
            Product updatedProduct = productRepository.save(existingProduct);
            return convertToDTO(updatedProduct);
        }).orElse(null); // If the product with the given ID does not exist, return null (or you could throw an exception)
    }

    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setQuantity(product.getQuantity());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setDescription(product.getDescription());
        return dto;
    }
}
