package com.vog.example.vog_tmf.adapter;

/** Shape of vog-demo's CategoryResponse — the legacy contract we adapt. */
public record LegacyCategory(Long id, String name, String description) {
}
