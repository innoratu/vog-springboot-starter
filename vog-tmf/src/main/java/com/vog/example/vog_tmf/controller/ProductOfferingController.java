package com.vog.example.vog_tmf.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vog.example.vog_tmf.dto.ProductOfferingCreate;
import com.vog.example.vog_tmf.dto.ProductOfferingTmf;
import com.vog.example.vog_tmf.service.ProductOfferingService;
import com.vog.example.vog_tmf.tmf.FieldsFilter;
import com.vog.example.vog_tmf.tmf.PageWindow;
import com.vog.example.vog_tmf.tmf.TmfApi;

import jakarta.validation.Valid;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping(TmfApi.BASE_PATH + "/productOffering")
public class ProductOfferingController {

    private final ProductOfferingService service;
    private final ObjectMapper mapper;

    public ProductOfferingController(ProductOfferingService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ObjectNode>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lifecycleStatus,
            @RequestParam(required = false) String fields,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        PageWindow<ProductOfferingTmf> page = service.list(name, lifecycleStatus, offset, limit);
        List<ObjectNode> body = page.items().stream()
                .map(item -> FieldsFilter.apply(mapper, item, fields))
                .toList();
        HttpStatus status = body.size() < page.total() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status)
                .header("X-Total-Count", String.valueOf(page.total()))
                .header("X-Result-Count", String.valueOf(body.size()))
                .body(body);
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable Long id, @RequestParam(required = false) String fields) {
        return FieldsFilter.apply(mapper, service.get(id), fields);
    }

    @PostMapping
    public ResponseEntity<ProductOfferingTmf> create(@Valid @RequestBody ProductOfferingCreate request) {
        ProductOfferingTmf created = service.create(request);
        return ResponseEntity.created(URI.create(created.href())).body(created);
    }

    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
    public ProductOfferingTmf patch(@PathVariable Long id, @RequestBody JsonNode patch) {
        return service.patch(id, patch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
