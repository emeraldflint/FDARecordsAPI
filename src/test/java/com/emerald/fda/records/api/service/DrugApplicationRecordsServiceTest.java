package com.emerald.fda.records.api.service;

import com.emerald.fda.records.api.dto.fda.DrugApplicationResultDto;
import com.emerald.fda.records.api.dto.fda.FdaResponseDto;
import com.emerald.fda.records.api.dto.fda.MetaDto;
import com.emerald.fda.records.api.dto.fda.OpenFdaDto;
import com.emerald.fda.records.api.dto.fda.ProductDto;
import com.emerald.fda.records.api.dto.fda.ResultsMetaDto;
import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import com.emerald.fda.records.api.repository.DrugApplicationRecordRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DrugApplicationRecordsServiceTest {
    @Mock
    private DrugApplicationRecordRepository repository;

    @Mock
    private FdaClientService fdaClientService;

    @InjectMocks
    private DrugApplicationRecordsService service;

    @Test
    void searchDrugApplications_ShouldDelegateToDdaClientService() {
        // Arrange
        var expectedResponse = new FdaResponseDto(
                new MetaDto(null, null, null, null, new ResultsMetaDto(0, 10, 1)),
                List.of(new DrugApplicationResultDto(null, "ANDA076805", "TARO", null, null))
        );

        when(fdaClientService.searchDrugApplicationRecords("TARO", "LORATADINE", 0, 10))
                .thenReturn(expectedResponse);

        // Act
        FdaResponseDto actualResponse = service.searchDrugApplicationRecords("TARO", "LORATADINE", 0, 10);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(fdaClientService).searchDrugApplicationRecords("TARO", "LORATADINE", 0, 10);
    }

    @Test
    void saveDrugApplication_WithNewApplication_ShouldCreateNewEntity() {
        // Arrange
        String applicationNumber = "ANDA076805";
        String manufacturerName = "TARO";
        String substanceName = "LORATADINE";
        var productNumbers = Set.of("001", "002");

        when(repository.findById(applicationNumber)).thenReturn(Optional.empty());
        when(repository.save(any(DrugApplicationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.saveDrugApplicationRecord(
                applicationNumber, manufacturerName, substanceName, productNumbers);

        // Assert
        assertThat(result.getApplicationNumber()).isEqualTo(applicationNumber);
        assertThat(result.getManufacturerName()).isEqualTo(manufacturerName);
        assertThat(result.getSubstanceName()).isEqualTo(substanceName);
        assertThat(result.getProductNumbers()).isEqualTo(productNumbers);

        verify(repository).findById(applicationNumber);
        verify(repository).save(any(DrugApplicationRecord.class));
    }

    @Test
    void saveDrugApplication_WithExistingApplication_ShouldUpdateEntity() {
        // Arrange
        String applicationNumber = "ANDA076805";
        String manufacturerName = "UPDATED MANUFACTURER";
        String substanceName = "UPDATED SUBSTANCE";
        Set<String> productNumbers = Set.of("002", "003");

        var existingApplication = DrugApplicationRecord.builder()
                .applicationNumber(applicationNumber)
                .manufacturerName("TARO")
                .substanceName("LORATADINE")
                .productNumbers(new HashSet<>(Set.of("001")))
                .build();

        when(repository.findById(applicationNumber)).thenReturn(Optional.of(existingApplication));
        when(repository.save(any(DrugApplicationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.saveDrugApplicationRecord(
                applicationNumber, manufacturerName, substanceName, productNumbers);

        // Assert
        assertThat(result.getApplicationNumber()).isEqualTo(applicationNumber);
        assertThat(result.getManufacturerName()).isEqualTo(manufacturerName);
        assertThat(result.getSubstanceName()).isEqualTo(substanceName);
        assertThat(result.getProductNumbers()).containsExactlyInAnyOrder("001", "002", "003");

        verify(repository).findById(applicationNumber);
        verify(repository).save(existingApplication);
    }

    @Test
    void saveDrugApplicationFromFdaResult_ShouldExtractFieldsAndSave() {
        // Arrange
        var openFdaDto = new OpenFdaDto(
                List.of("ANDA076805"),
                List.of("BRAND"),
                List.of("GENERIC"),
                List.of("TARO"),
                null, null, null,
                List.of("LORATADINE"),
                null, null, null, null, null
        );

        var product1 = new ProductDto("001", null, null, null, null, null, null, null);
        var product2 = new ProductDto("002", null, null, null, null, null, null, null);

        when(repository.findById(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(DrugApplicationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.saveDrugApplicationRecord("ANDA076805", "TARO", "LORATADINE", Set.of("001", "002"));

        // Assert
        assertThat(result.getApplicationNumber()).isEqualTo("ANDA076805");
        assertThat(result.getManufacturerName()).isEqualTo("TARO");
        assertThat(result.getSubstanceName()).isEqualTo("LORATADINE");
        assertThat(result.getProductNumbers()).containsExactlyInAnyOrder("001", "002");

        verify(repository).save(any(DrugApplicationRecord.class));
    }

    @Test
    void getAllDrugApplications_ShouldReturnPageFromRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<DrugApplicationRecord> applications = List.of(
                DrugApplicationRecord.builder()
                        .applicationNumber("ANDA076805")
                        .manufacturerName("TARO")
                        .substanceName("LORATADINE")
                        .productNumbers(Set.of("001"))
                        .build(),
                DrugApplicationRecord.builder()
                        .applicationNumber("ANDA076806")
                        .manufacturerName("OTHER")
                        .substanceName("SUBSTANCE")
                        .productNumbers(Set.of("002"))
                        .build()
        );

        Page<DrugApplicationRecord> expectedPage = new PageImpl<>(applications, pageable, applications.size());
        when(repository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<DrugApplicationRecord> actualPage = service.getAllDrugApplicationRecords(pageable);

        // Assert
        assertThat(actualPage).isEqualTo(expectedPage);
        assertThat(actualPage.getContent()).hasSize(2);
        verify(repository).findAll(pageable);
    }

    @Test
    void getDrugApplicationById_ShouldReturnApplicationFromRepository() {
        // Arrange
        String applicationNumber = "ANDA076805";
        var expectedApplication = DrugApplicationRecord.builder()
                .applicationNumber(applicationNumber)
                .manufacturerName("TARO")
                .substanceName("LORATADINE")
                .productNumbers(Set.of("001"))
                .build();

        when(repository.findById(applicationNumber)).thenReturn(Optional.of(expectedApplication));

        // Act
        Optional<DrugApplicationRecord> actualApplication = service.getDrugApplicationById(applicationNumber);

        // Assert
        assertThat(actualApplication).isPresent();
        assertThat(actualApplication.get()).isEqualTo(expectedApplication);
        verify(repository).findById(applicationNumber);
    }

    @Test
    void getDrugApplicationById_WithNonExistentId_ShouldReturnEmpty() {
        // Arrange
        String applicationNumber = "NONEXISTENT";
        when(repository.findById(applicationNumber)).thenReturn(Optional.empty());

        // Act
        Optional<DrugApplicationRecord> actualApplication = service.getDrugApplicationById(applicationNumber);

        // Assert
        assertThat(actualApplication).isEmpty();
        verify(repository).findById(applicationNumber);
    }
}