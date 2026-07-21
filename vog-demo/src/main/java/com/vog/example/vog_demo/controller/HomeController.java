package com.vog.example.vog_demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A friendly landing endpoint at the root path so hitting
 * {@code http://localhost:8080/} shows what the API offers instead of a 404.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "Vog — Living Things Catalog API",
                "status", "up",
                "endpoints", Map.of(
                        "categories", "/api/categories",
                        "organisms", "/api/organisms",
                        "swaggerUi", "/swagger-ui.html",
                        "openApiSpec", "/v3/api-docs",
                        "h2Console", "/h2-console"));
    }
}
