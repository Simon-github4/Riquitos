package com.riquitos.product;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return repository.findAll(Sort.by(Sort.Direction.ASC, "description"));
        } else {
            return productRepository.findByDescriptionContainingIgnoreCase(filterText);
        }
    }
    
    @Transactional(readOnly = true)
	public List<Product> findAllByDescriptionAscImgNotNullFirst() {
		//return repository.findAll(Sort.by(Sort.Direction.ASC, "description"));
		return productRepository.findAllOrderByImagePresentAndDescription();
	}
}
