package com.example.Shop.repositories;

import com.example.Shop.entities.Product;
import com.example.Shop.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByLastName(String lastName);
    Optional<Staff> findByPhone(String phone);
    Optional<Staff> findByEmail(String email);
}
