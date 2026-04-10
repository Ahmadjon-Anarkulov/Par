package com.parlament.repository;

import com.parlament.model.Category;
import com.parlament.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findByCategory(Category category);
    Optional<Product> findById(String id);
}

