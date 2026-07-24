package com.vog.example.vog_tmf.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.UpdateTimestamp;

import com.vog.example.vog_tmf.tmf.TimePeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TMF620 productOffering: what is sold, built on a productSpecification. */
@Entity
@Table(name = "tmf_product_offering")
@Getter
@Setter
@NoArgsConstructor
public class ProductOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private Boolean isBundle;

    private Boolean isSellable;

    private String lifecycleStatus;

    @ManyToOne
    @JoinColumn(name = "product_spec_id")
    private ProductSpecification productSpecification;

    @ManyToMany
    @JoinTable(name = "tmf_offering_category",
            joinColumns = @JoinColumn(name = "offering_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new LinkedHashSet<>();

    @Embedded
    private TimePeriod validFor;

    @UpdateTimestamp
    private Instant lastUpdate;
}
