package com.example.Shop.services;

import com.example.Shop.dto.StaffDTO;
import com.example.Shop.entities.Staff;
import com.example.Shop.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    public List<StaffDTO> getAllStaff() {
        // Implement logic to retrieve all staff members and convert them to DTOs
        return staffRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StaffDTO getWorkerById(Long id) {
        // Implement logic to retrieve a staff member by ID and convert to DTO
        return staffRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public StaffDTO createWorker(com.example.Shop.entities.Staff staff) {
        staff.setPhone(cleanString(staff.getPhone()));
        staff.setEmail(cleanString(staff.getEmail()));

        staffRepository .findByPhone(staff.getPhone()).ifPresent(s -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone " + s.getPhone() + " already exists for staff with ID " + s.getId());
        });
        if (staff.getEmail() != null) {
            staffRepository.findByEmail(staff.getEmail()).ifPresent(s -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            });
        }

        Staff savedStaff = staffRepository.save(staff);
        return convertToDTO(savedStaff);
    }

    private String cleanString(String input) {
        if (input == null) return null;

        String trimmed = input.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    public StaffDTO updateWorker(Long id, com.example.Shop.entities.Staff staffDetails) {
        return staffRepository.findById(id).map(existingStaff -> {
            existingStaff.setFirstName(staffDetails.getFirstName());
            existingStaff.setLastName(staffDetails.getLastName());
            existingStaff.setPhone(staffDetails.getPhone());
            existingStaff.setEmail(staffDetails.getEmail());

            Staff updatedStaff = staffRepository.save(existingStaff);
            return convertToDTO(updatedStaff);
        }).orElse(null);
    }

    public boolean deleteWorker(Long id) {
        if (staffRepository.existsById(id)) {
            staffRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private StaffDTO convertToDTO(com.example.Shop.entities.Staff staff) {
        // Implement logic to convert Staff entity to StaffDTO
        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setPhone(staff.getPhone());
        dto.setEmail(staff.getEmail());
        return dto;
    }
}
