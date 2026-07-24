package com.vog.example.vog_tmf.entity;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import com.vog.example.vog_tmf.tmf.TimePeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TMF620 product specification: describes a sellable product's characteristics. */
@Entity
@Table(name = "tmf_product_spec")
@Getter
@Setter
@NoArgsConstructor
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String brand;

    @Column(name = "spec_version")
    private String version;

    private String lifecycleStatus;

    @Embedded
    private TimePeriod validFor;

    @UpdateTimestamp
    private Instant lastUpdate;
}
