package com.vog.example.vog_demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vog.example.vog_demo.dto.OrganismRequest;
import com.vog.example.vog_demo.dto.OrganismResponse;
import com.vog.example.vog_demo.service.OrganismService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/organisms")
public class OrganismController {

    private final OrganismService organismService;

    public OrganismController(OrganismService organismService) {
        this.organismService = organismService;
    }

    @GetMapping
    public List<OrganismResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        return organismService.list(categoryId, name);
    }

    @GetMapping("/{id}")
    public OrganismResponse get(@PathVariable Long id) {
        return organismService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganismResponse create(@Valid @RequestBody OrganismRequest request) {
        return organismService.create(request);
    }

    @PutMapping("/{id}")
    public OrganismResponse update(@PathVariable Long id, @Valid @RequestBody OrganismRequest request) {
        return organismService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organismService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
