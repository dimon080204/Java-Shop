package com.example.Shop.services;

import com.example.Shop.dto.DummyJsonResponse;
import com.example.Shop.dto.ExternalProductDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
public class ImportService {

    public BigDecimal getLiveUsdRate() {
        try {
            Document doc = Jsoup.connect("https://minfin.com.ua/currency/usd/")
                    .userAgent("Mozilla/5.0")
                    .get();

            Element rateElement = doc.select("table.sc-1x32wa2-1 td.sc-1x32wa2-8").get(1);

            String rawText = rateElement.ownText();
            if (rawText.isEmpty()) {
                rawText = rateElement.text().split(" ")[0];
            }

            String cleanRate = rawText.replaceAll("[^0-9,.]", "").replace(",", ".");

            return new BigDecimal(cleanRate);
        } catch (Exception e) {
            return new BigDecimal("44.50");
        }
    }

    private final String DUMMY_URL = "https://dummyjson.com/products/search?q=";

    public List<ExternalProductDTO> searchExternalProducts(String query) {
        RestTemplate restTemplate = new RestTemplate();
        DummyJsonResponse response = restTemplate.getForObject(DUMMY_URL + query, DummyJsonResponse.class);

        if (response != null && response.getProducts() != null) {
            BigDecimal currentRate = getLiveUsdRate();

            for (ExternalProductDTO product : response.getProducts()) {
                if (product.getPrice() != null) {
                    BigDecimal priceInUah = product.getPrice().multiply(currentRate)
                            .setScale(2, RoundingMode.HALF_UP);

                    product.setDescription(product.getDescription() +
                            " [Price in UAH: " + priceInUah + " by course " + currentRate + "]");
                    product.setPrice(priceInUah);
                }
            }
            return response.getProducts();
        }
        return Collections.emptyList();
    }
}
