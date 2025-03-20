package com.emerald.fda.records.api.controller;

import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.dto.request.StoreDrugApplicationRecordDto;
import com.emerald.fda.records.api.dto.response.PageResponseDto;
import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import com.emerald.fda.records.api.service.DrugApplicationRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Drug Applications", description = "API for managing drug application records")
public class DrugApplicationRecordsController {

    private final DrugApplicationRecordsService service;

    /**
     * Searches for drug applications in the FDA database.
     */
    @GetMapping("/search")
    @Operation(summary = "Search drug applications in FDA database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
            @ApiResponse(responseCode = "503", description = "FDA API unavailable")
    })
    public ResponseEntity<FdaResponseDto> searchDrugApplicationRecord(
            @Parameter(description = "FDA manufacturer name", required = true)
            @RequestParam String manufacturerName,

            @Parameter(description = "FDA brand name (optional)")
            @RequestParam(required = false) String brandName,

            @Parameter(description = "Number of results to skip")
            @RequestParam(defaultValue = "0") @Min(0) int skip,

            @Parameter(description = "Maximum number of results to return")
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        log.info("Received request to search drug application records with manufacturer: {}, brand: {}",
                manufacturerName, brandName);

        FdaResponseDto response = service.searchDrugApplicationRecords(manufacturerName, brandName, skip, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Stores specific drug application details in the system.
     */
    @PostMapping
    @Operation(summary = "Store specific drug application record details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Drug application record stored successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid drug application record data provided")
    })
    public ResponseEntity<DrugApplicationRecord> storeSpecificDrugApplicationRecord(
            @Parameter(description = "Specific drug application record details to store", required = true)
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
    @Operation(summary = "Get all drug application records stored in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved drug application records")
    })
    public ResponseEntity<PageResponseDto<DrugApplicationRecord>> getAllDrugApplicationRecords(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
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
    @Operation(summary = "Get a drug application record by its application number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the drug application record"),
            @ApiResponse(responseCode = "404", description = "Drug application record not found")
    })
    public ResponseEntity<DrugApplicationRecord> getDrugApplicationRecordById(
            @Parameter(description = "Application number", required = true)
            @PathVariable String applicationNumber) {

        log.info("Received request to get drug application record by ID: {}", applicationNumber);

        return service.getDrugApplicationById(applicationNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}