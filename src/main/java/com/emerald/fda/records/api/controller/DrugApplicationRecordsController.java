package com.emerald.fda.records.api.controller;

import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.dto.request.StoreDrugApplicationRecordDto;
import com.emerald.fda.records.api.dto.response.PageResponseDto;
import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import com.emerald.fda.records.api.service.DrugApplicationRecordsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for drug application operations.
 */
@RestController
@RequestMapping("/v1/drug-application-records")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DrugApplicationRecordsController {

    private final DrugApplicationRecordsService service;

    /**
     * Searches for drug applications in the FDA database.
     */
    @GetMapping("/search")
    public ResponseEntity<FdaResponseDto> searchDrugApplicationRecord(
            @RequestParam String manufacturerName,
            @RequestParam(required = false) String brandName,
            @RequestParam(defaultValue = "0") @Min(0) int skip,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        log.info("Received request to search drug applications with manufacturer: {}, brand: {}",
                manufacturerName, brandName);

        FdaResponseDto response = service.searchDrugApplicationRecords(manufacturerName, brandName, skip, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Stores specific drug application details in the system.
     */
    @PostMapping
    public ResponseEntity<DrugApplicationRecord> storeSpecificDrugApplicationRecord(
            @RequestBody @Valid StoreDrugApplicationRecordDto applicationDto) {

        log.info("Received request to store specific drug application: {}", applicationDto.applicationNumber());

        DrugApplicationRecord savedApplication = service.saveDrugApplicationRecord(
                applicationDto.applicationNumber(),
                applicationDto.manufacturerName(),
                applicationDto.substanceName(),
                applicationDto.productNumbers()
        );

        return new ResponseEntity<>(savedApplication, HttpStatus.CREATED);
    }

    /**
     * Gets all drug applications stored in the system.
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<DrugApplicationRecord>> getAllDrugApplicationRecords(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        log.info("Received request to get all drug applications, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                PageResponseDto.from(service.getAllDrugApplicationRecords(pageable))
        );
    }

    /**
     * Gets a drug application by its application number.
     */
    @GetMapping("/{applicationNumber}")
    public ResponseEntity<DrugApplicationRecord> getDrugApplicationRecordById(
            @PathVariable String applicationNumber) {

        log.info("Received request to get drug application by ID: {}", applicationNumber);

        return service.getDrugApplicationById(applicationNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}