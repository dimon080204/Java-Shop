package com.example.Shop.services;

import com.example.Shop.dto.StaffKPIDTO;
import com.example.Shop.entities.Sale;
import com.example.Shop.entities.Staff;
import com.example.Shop.repositories.SaleRepository;
import com.example.Shop.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KPIService {

    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private StaffRepository staffRepository;

    public List<StaffKPIDTO> calculateStaffKPI(LocalDateTime start, LocalDateTime end) {
        List<Staff> allStaff = staffRepository.findAll();
        List<Sale> allSales = saleRepository.findBySaleDateBetween(start, end);

        // 1. Собираем базовую статистику по каждому сотруднику
        List<StaffKPIDTO> dtos = allStaff.stream().map(worker -> {
            List<Sale> workerSales = allSales.stream()
                    .filter(s -> s.getStaff().getId().equals(worker.getId()))
                    .collect(Collectors.toList());

            BigDecimal revenue = workerSales.stream()
                    .map(Sale::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = workerSales.size();
            BigDecimal avgCheck = count > 0
                    ? revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            StaffKPIDTO dto = new StaffKPIDTO();
            dto.setStaffId(worker.getId());
            dto.setFullName(worker.getLastName() + " " + worker.getFirstName().substring(0, 1) + ".");
            dto.setTotalRevenue(revenue);
            dto.setSalesCount(count);
            dto.setAverageCheck(avgCheck);
            return dto;
        }).collect(Collectors.toList());

        // 2. Считаем средние показатели по магазину для нормировки
        double avgRev = dtos.stream().mapToDouble(d -> d.getTotalRevenue().doubleValue()).average().orElse(1.0);
        double avgCnt = dtos.stream().mapToLong(StaffKPIDTO::getSalesCount).average().orElse(1.0);
        double avgChk = dtos.stream().mapToDouble(d -> d.getAverageCheck().doubleValue()).average().orElse(1.0);

        // 3. Рассчитываем финальный KPI Score (взвешенный коэффициент)
        for (StaffKPIDTO d : dtos) {
            double score = (0.5 * (d.getTotalRevenue().doubleValue() / (avgRev > 0 ? avgRev : 1))) +
                    (0.3 * (d.getSalesCount() / (avgCnt > 0 ? avgCnt : 1))) +
                    (0.2 * (d.getAverageCheck().doubleValue() / (avgChk > 0 ? avgChk : 1)));
            d.setKpiScore(Math.round(score * 100.0) / 100.0);
        }

        // Сортируем: лучшие продавцы вверху
        dtos.sort((a, b) -> b.getKpiScore().compareTo(a.getKpiScore()));
        return dtos;
    }
}