package com.example.Shop.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(schema = "store", name = "purchases")
@Data
public class Purchase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // The cost is calculated as price * quantity
    // This field should be ignored in write requests and only returned in read responses
    @Column(name = "cost")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal cost;

    @CreationTimestamp
    @Column(name = "purchase_date", nullable = false)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime purchaseDate;

    public Purchase() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Purchase purchase = (Purchase) o;
        return Objects.equals(id, purchase.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Purchases{" +
                "id=" + id +
                ", product=" + product +
                ", staff=" + staff +
                ", quantity=" + quantity +
                ", purchaseDate=" + purchaseDate +
                ", price=" + price +
                ", cost=" + cost +
                '}';
    }
}
