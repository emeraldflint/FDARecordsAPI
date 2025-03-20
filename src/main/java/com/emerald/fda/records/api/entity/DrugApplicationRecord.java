package com.emerald.fda.records.api.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a drug application record stored in the system.
 */
@Entity
@Table(name = "drug_application_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugApplicationRecord {
    /**
     * Application number, used as the primary key.
     */
    @Id
    @Column(name = "application_number", nullable = false, unique = true)
    private String applicationNumber;

    /**
     * Name of the manufacturer.
     */
    @Column(name = "manufacturer_name", nullable = false)
    private String manufacturerName;

    /**
     * Name of the substance.
     */
    @Column(name = "substance_name", nullable = false)
    private String substanceName;

    /**
     * Collection of product numbers associated with this application.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_numbers",
            joinColumns = @JoinColumn(name = "application_number")
    )
    @Column(name = "product_number", nullable = false)
    @Builder.Default
    private Set<String> productNumbers = new HashSet<>();
}
