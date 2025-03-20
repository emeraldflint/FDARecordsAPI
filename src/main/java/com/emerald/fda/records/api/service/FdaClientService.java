package com.emerald.fda.records.api.service;

import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for communicating with the FDA API.
 */
@Service
@Slf4j
public class FdaClientService {
    private final RestTemplate restTemplate;
    private final String openFdaBaseUrl;

    public FdaClientService(RestTemplate restTemplate,
                            @Value("${fda.api.base-url}") String openFdaBaseUrl) {
        this.restTemplate = restTemplate;
        this.openFdaBaseUrl = openFdaBaseUrl;
    }

    /**
     * Searches for drug applications records in the OpenFDA API.
     *
     * @param manufacturerName The manufacturer name to search for
     * @param brandName The optional brand name to search for
     * @return A {@link FdaResponseDto} object containing the search results
     */
    public FdaResponseDto searchDrugApplicationRecords(
            String manufacturerName,
            String brandName,
            int skip,
            int limit) {

        log.info("Searching for drug applications with manufacturer: {}, brand: {}, skip: {}, limit: {}",
                manufacturerName, brandName, skip, limit);

        String searchQuery = buildSearchQuery(manufacturerName, brandName);
        String uri = UriComponentsBuilder.fromUriString(openFdaBaseUrl)
                .queryParam("search", searchQuery)
                .queryParam("skip", skip)
                .queryParam("limit", limit)
                .build()
                .toUriString();

        log.debug("OpenFDA API request URL: {}", uri);

        try {
            var response = restTemplate.getForObject(uri, FdaResponseDto.class);

            log.info("Retrieved {} drug application records from OpenFDA API",
                    response != null && response.results() != null ? response.results().size() : 0);

            return response;
        } catch (Exception ex) {
            log.error("Error calling OpenFDA API: {}", ex.getMessage(), ex);
            throw new ServiceException("Failed to retrieve drug application records from OpenFDA API", ex);
        }
    }

    /**
     * Builds a search query string for the OpenFDA API based on manufacturer and brand
     *
     * @param manufacturerName The manufacturer name to search for (required)
     * @param brandName        The brand name to search for (optional)
     * @return A formatted search query string
     */
    public String buildSearchQuery(String manufacturerName, String brandName) {
        StringBuilder query = new StringBuilder();

        // Add manufacturer name condition (required)
        query.append("openfda.manufacturer_name:\"").append(manufacturerName).append("\"");

        // Add brand name condition if provided
        if (StringUtils.hasText(brandName)) {
            query.append(" AND openfda.brand_name:\"").append(brandName).append("\"");
        }

        return query.toString();
    }
}
