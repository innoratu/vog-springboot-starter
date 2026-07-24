package com.vog.example.vog_tmf.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vog.example.vog_tmf.entity.Category;
import com.vog.example.vog_tmf.entity.ProductOffering;
import com.vog.example.vog_tmf.entity.ProductSpecification;
import com.vog.example.vog_tmf.repository.CategoryRepository;
import com.vog.example.vog_tmf.repository.ProductOfferingRepository;
import com.vog.example.vog_tmf.repository.ProductSpecificationRepository;

/** Seeds a small telco catalog so every tutorial curl returns real data. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(CategoryRepository categories, ProductSpecificationRepository specs,
            ProductOfferingRepository offerings) {
        return args -> {
            if (categories.count() > 0) {
                return;
            }
            Category mobile = categories.save(category("Mobile", "Mobile subscriptions and add-ons"));
            Category internet = categories.save(category("Internet", "Home and business internet"));
            Category business = categories.save(category("Business", "Offers for business customers"));

            ProductSpecification sim = specs.save(spec("5G SIM-Only Spec", "SIM-only 5G plan blueprint"));
            ProductSpecification fibre = specs.save(spec("Fibre 1G Spec", "1 Gbps fibre access blueprint"));

            offerings.save(offering("Mobile 5G Unlimited", sim, mobile));
            offerings.save(offering("Mobile 5G Basic", sim, mobile));
            offerings.save(offering("Fibre Gigabit Home", fibre, internet));
            ProductOffering biz = offering("Business Fibre 1G", fibre, internet);
            biz.getCategories().add(business);
            offerings.save(biz);
        };
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        c.setLifecycleStatus("Active");
        c.setIsRoot(true);
        return c;
    }

    private ProductSpecification spec(String name, String description) {
        ProductSpecification s = new ProductSpecification();
        s.setName(name);
        s.setDescription(description);
        s.setLifecycleStatus("Active");
        return s;
    }

    private ProductOffering offering(String name, ProductSpecification spec, Category category) {
        ProductOffering o = new ProductOffering();
        o.setName(name);
        o.setLifecycleStatus("Active");
        o.setIsBundle(false);
        o.setIsSellable(true);
        o.setProductSpecification(spec);
        o.getCategories().add(category);
        return o;
    }
}
