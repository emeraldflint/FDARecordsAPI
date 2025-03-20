package com.emerald.fda.records.api.repository;

import com.emerald.fda.records.api.entity.DrugApplicationRecord;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DrugApplicationRecordRepositoryTest {
    @Autowired
    private DrugApplicationRecordRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void saveAndFindById_ShouldSaveAndRetrieveDrugApplication() {
        // given
        var application = createDrugApplication("ANDA076805", "TARO", "LORATADINE", Set.of("001"));

        // when
        repository.save(application);
        var foundApplication = repository.findById("ANDA076805");

        // then
        assertThat(foundApplication).isPresent()
                .get()
                .satisfies(app -> {
                    assertThat(app.getApplicationNumber()).isEqualTo("ANDA076805");
                    assertThat(app.getManufacturerName()).isEqualTo("TARO");
                    assertThat(app.getSubstanceName()).isEqualTo("LORATADINE");
                    assertThat(app.getProductNumbers()).containsExactlyInAnyOrder("001");
                });
    }

    @Test
    void findAll_ShouldReturnAllDrugApplications() {
        // given
        var application1 = createDrugApplication("ANDA076805", "TARO", "LORATADINE", Set.of("001"));
        var application2 = createDrugApplication("ANDA076806", "OTHER", "SUBSTANCE", Set.of("002"));

        repository.save(application1);
        repository.save(application2);

        // when
        var applications = repository.findAll(PageRequest.of(0, 10));

        // then
        assertThat(applications.getTotalElements()).isEqualTo(2);
        assertThat(applications.getContent()).extracting(DrugApplicationRecord::getApplicationNumber)
                .containsExactlyInAnyOrder("ANDA076805", "ANDA076806");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // when
        var result = repository.findById("NON_EXISTENT_ID");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void saveDuplicateId_ShouldUpdateExistingRecord() {
        // given
        var original = createDrugApplication("ANDA076805", "TARO", "LORATADINE", Set.of("001"));
        repository.save(original);

        var updated = createDrugApplication("ANDA076805", "NEW_MANUFACTURER", "NEW_SUBSTANCE", Set.of("002"));
        repository.save(updated);

        // when
        var foundApplication = repository.findById("ANDA076805");

        // then
        assertThat(foundApplication).isPresent()
                .get()
                .satisfies(app -> {
                    assertThat(app.getManufacturerName()).isEqualTo("NEW_MANUFACTURER");
                    assertThat(app.getSubstanceName()).isEqualTo("NEW_SUBSTANCE");
                    assertThat(app.getProductNumbers()).containsExactlyInAnyOrder("002");
                });
    }

    private DrugApplicationRecord createDrugApplication(String applicationNumber, String manufacturer, String substance, Set<String> productNumbers) {
        return DrugApplicationRecord.builder()
                .applicationNumber(applicationNumber)
                .manufacturerName(manufacturer)
                .substanceName(substance)
                .productNumbers(productNumbers)
                .build();
    }
}