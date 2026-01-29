package com.riquitos.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.riquitos.AbstractCrudService;

@Service
public class ProductService extends AbstractCrudService<Product, Long> {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository repository) {
        super(repository); 
        this.productRepository = repository;
    }

    @Override
    public List<Product> findAll(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return repository.findAll();
        } else {
            return productRepository.findByDescriptionContainingIgnoreCase(filterText);
        }
    }
}
