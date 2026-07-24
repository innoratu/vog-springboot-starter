package com.vog.example.vog_tmf.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vog.example.vog_tmf.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
