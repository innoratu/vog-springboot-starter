package com.vog.example.vog_tmf.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vog.example.vog_tmf.entity.ProductSpecification;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
}
