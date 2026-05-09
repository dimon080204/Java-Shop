package com.example.Shop.services;

import com.example.Shop.dto.AnalyticsDTO;
import com.example.Shop.entities.Purchase;
import com.example.Shop.entities.Sale;
import com.example.Shop.repositories.PurchaseRepository;
import com.example.Shop.repositories.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private SaleRepository saleRepository;

    public List<AnalyticsDTO> getABCAnalysis(LocalDateTime start, LocalDateTime end) {
        // 1. Получаем все данные за период
        List<Purchase> periodPurchases = purchaseRepository.findByPurchaseDateBetween(start, end);
        List<Sale> periodSales = saleRepository.findBySaleDateBetween(start, end);

        // 2. Считаем среднюю себестоимость для каждого товара
        Map<Long, BigDecimal> avgCosts = periodPurchases.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProduct().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long totalQty = list.stream().mapToLong(Purchase::getQuantity).sum();
                                    BigDecimal totalCost = list.stream()
                                            .map(Purchase::getCost)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return totalQty > 0 ? totalCost.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                                }
                        )
                ));

        // 3. Группируем продажи по товарам и считаем прибыль
        Map<Long, AnalyticsDTO> analyticsMap = new HashMap<>();

        for (Sale sale : periodSales) {
            Long pId = sale.getProduct().getId();
            AnalyticsDTO dto = analyticsMap.getOrDefault(pId, new AnalyticsDTO());
            dto.setProductId(pId);
            dto.setProductName(sale.getProduct().getName());

            BigDecimal costPrice = avgCosts.getOrDefault(pId, BigDecimal.ZERO).multiply(BigDecimal.valueOf(sale.getQuantity()));
            BigDecimal saleProfit = sale.getTotalPrice().subtract(costPrice);

            dto.setProfit(dto.getProfit() == null ? saleProfit : dto.getProfit().add(saleProfit));
            analyticsMap.put(pId, dto);
        }

        List<AnalyticsDTO> result = new ArrayList<>(analyticsMap.values());

        // 4. Сортируем по прибыли для ABC
        result.sort((a, b) -> b.getProfit().compareTo(a.getProfit()));

        // 5. Присваиваем категории A, B, C
        BigDecimal totalProfit = result.stream()
                .map(AnalyticsDTO::getProfit)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal runningSum = BigDecimal.ZERO;
        for (AnalyticsDTO item : result) {
            if (totalProfit.compareTo(BigDecimal.ZERO) > 0 && item.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                runningSum = runningSum.add(item.getProfit());
                double cumulativeShare = runningSum.divide(totalProfit, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                item.setShare(cumulativeShare);

                if (cumulativeShare <= 80) item.setAbcCategory("A");
                else if (cumulativeShare <= 95) item.setAbcCategory("B");
                else item.setAbcCategory("C");
            } else {
                item.setAbcCategory("C");
                item.setShare(100.0);
            }
        }

        return result;
    }
}