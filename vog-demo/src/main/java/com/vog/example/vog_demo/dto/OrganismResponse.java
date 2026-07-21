package com.vog.example.vog_demo.dto;

import com.vog.example.vog_demo.entity.Organism;

public record OrganismResponse(
        Long id,
        String commonName,
        String scientificName,
        String habitat,
        String description,
        Long categoryId,
        String categoryName) {

    public static OrganismResponse from(Organism organism) {
        return new OrganismResponse(
                organism.getId(),
                organism.getCommonName(),
                organism.getScientificName(),
                organism.getHabitat(),
                organism.getDescription(),
                organism.getCategory().getId(),
                organism.getCategory().getName());
    }
}
