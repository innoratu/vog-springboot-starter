package com.vog.example.vog_tmf.adapter;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vog.example.vog_tmf.dto.CategoryTmf;
import com.vog.example.vog_tmf.tmf.TmfApi;

/**
 * Retrofit facade: exposes vog-demo's legacy category API as read-only TMF620
 * category resources. Demonstrates the adapter strategy from the tutorial's
 * migration playbook.
 */
@RestController
@RequestMapping(TmfApi.BASE_PATH + "/legacyCategory")
public class LegacyCategoryAdapterController {

    private final LegacyCatalogClient client;

    public LegacyCategoryAdapterController(LegacyCatalogClient client) {
        this.client = client;
    }

    @GetMapping
    public List<CategoryTmf> list() {
        return client.fetchCategories().stream().map(this::toTmf).toList();
    }

    @GetMapping("/{id}")
    public CategoryTmf get(@PathVariable long id) {
        return toTmf(client.fetchCategory(id));
    }

    /** The legacy model has no lifecycle/validFor — the adapter chooses safe defaults. */
    private CategoryTmf toTmf(LegacyCategory legacy) {
        String id = String.valueOf(legacy.id());
        return new CategoryTmf(id, TmfApi.BASE_PATH + "/legacyCategory/" + id, "Category",
                legacy.name(), legacy.description(), "Active", true, null, null, null);
    }
}
