package com.parlament.repository;

import com.parlament.data.ProductCatalog;
import com.parlament.model.Category;
import com.parlament.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    @Override
    public List<Product> findByCategory(Category category) {
        return ProductCatalog.findByCategory(category);
    }

    @Override
    public Optional<Product> findById(String id) {
        return ProductCatalog.findById(id);
    }
}

