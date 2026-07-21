package com.vog.example.vog_demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vog.example.vog_demo.entity.Organism;

public interface OrganismRepository extends JpaRepository<Organism, Long> {

    List<Organism> findByCategoryId(Long categoryId);

    List<Organism> findByCommonNameContainingIgnoreCase(String name);

    List<Organism> findByCategoryIdAndCommonNameContainingIgnoreCase(Long categoryId, String name);

    boolean existsByCategoryId(Long categoryId);
}
