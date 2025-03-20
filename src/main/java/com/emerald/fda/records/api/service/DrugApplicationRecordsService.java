package com.emerald.fda.records.api.service;

import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import com.emerald.fda.records.api.repository.DrugApplicationRecordRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for managing drug applications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DrugApplicationRecordsService {
    private final DrugApplicationRecordRepository repository;
    private final FdaClientService fdaClientService;

    /**
     * Searches for drug applications in the FDA database.
     *
     * @param manufacturerName The manufacturer name to search for
     * @param brandName        The optional brand name to search for
     * @param skip             The number of results to skip
     * @param limit            The maximum number of results to return
     * @return A {@link FdaResponseDto} object containing the search results
     */
    public FdaResponseDto searchDrugApplicationRecords(
            String manufacturerName,
            String brandName,
            int skip,
            int limit) {

        return fdaClientService.searchDrugApplicationRecords(manufacturerName, brandName, skip, limit);
    }

    /**
     * Saves a drug application to the database.
     *
     * @param applicationNumber The application number of the drug application
     * @param manufacturerName  The manufacturer name of the drug application
     * @param substanceName     The substance name of the drug application
     * @param productNumbers    The product numbers of the drug application
     * @return The saved drug application
     */
    @Transactional
    public DrugApplicationRecord saveDrugApplicationRecord(
            String applicationNumber,
            String manufacturerName,
            String substanceName,
            Set<String> productNumbers) {

        log.info("Saving drug application with number: {}", applicationNumber);

        var existingApplication = repository.findById(applicationNumber);

        if (existingApplication.isPresent()) {
            log.info("Updating existing drug application: {}", applicationNumber);

            var application = existingApplication.get();
            application.setManufacturerName(manufacturerName);
            application.setSubstanceName(substanceName);
            application.getProductNumbers().addAll(productNumbers);

            return repository.save(application);
        } else {
            log.info("Creating new drug application: {}", applicationNumber);

            var newApplication = DrugApplicationRecord.builder()
                    .applicationNumber(applicationNumber)
                    .manufacturerName(manufacturerName)
                    .substanceName(substanceName)
                    .productNumbers(productNumbers)
                    .build();

            return repository.save(newApplication);
        }
    }

    /**
     * Gets all drug applications with pagination.
     *
     * @param pageable The pagination information
     * @return A {@link Page} object containing the drug applications
     */
    public Page<DrugApplicationRecord> getAllDrugApplicationRecords(Pageable pageable) {
        log.info("Getting all drug applications with page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return repository.findAll(pageable);
    }

    /**
     * Gets a drug application by its application number.
     *
     * @param applicationNumber The application number of the drug application
     * @return An {@link Optional} object containing the drug application
     */
    public Optional<DrugApplicationRecord> getDrugApplicationById(String applicationNumber) {
        log.info("Getting drug application by ID: {}", applicationNumber);

        return repository.findById(applicationNumber);
    }
}
