package com.vog.example.vog_tmf.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vog.example.vog_tmf.entity.ProductOffering;

public interface ProductOfferingRepository extends JpaRepository<ProductOffering, Long> {

    boolean existsByCategoriesId(Long categoryId);

    boolean existsByProductSpecificationId(Long specId);
}
