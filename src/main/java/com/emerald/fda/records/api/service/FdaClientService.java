package com.emerald.fda.records.api.service;

import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for communicating with the FDA API.
 */
@Service
@Slf4j
public class FdaClientService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FdaClientService(RestTemplate restTemplate,
                            @Value("${fda.base.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Searches for drug applications in the OpenFDA API.
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

        var builder = UriComponentsBuilder.fromUriString(baseUrl);

        // Build the search query
        var searchQuery = buildSearchQuery(manufacturerName, brandName);

        // Add query parameters
        builder.queryParam("search", searchQuery);
        builder.queryParam("skip", skip);
        builder.queryParam("limit", limit);

        var url = builder.toUriString();
        log.debug("FDA API request URL: {}", url);

        // Make the request to the FDA API
        FdaResponseDto response = restTemplate.getForObject(url, FdaResponseDto.class);

        if (response != null && response.results() != null) {
            log.info("Found {} drug applications", response.results().size());
        } else {
            log.warn("No drug applications found or response is null");
        }

        return response;
    }

    /**
     * Build the search query string for the OpenFDA API.
     *
     * @param manufacturerName The manufacturer name to search for
     * @param brandName The optional brand name to search for
     * @return The search query string
     */
    private String buildSearchQuery(String manufacturerName, String brandName) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("openfda.manufacturer_name:\"")
                .append(manufacturerName)
                .append("\"");

        if (brandName != null && !brandName.trim().isEmpty()) {
            queryBuilder.append("+AND+openfda.brand_name:\"")
                    .append(brandName)
                    .append("\"");
        }

        return queryBuilder.toString();
    }
}
