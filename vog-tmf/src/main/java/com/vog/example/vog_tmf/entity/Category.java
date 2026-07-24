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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TMF620 category: groups product offerings; may nest via parent. */
@Entity
@Table(name = "tmf_category")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String lifecycleStatus;

    private Boolean isRoot;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Embedded
    private TimePeriod validFor;

    @UpdateTimestamp
    private Instant lastUpdate;
}
