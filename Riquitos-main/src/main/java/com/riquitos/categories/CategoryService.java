package com.riquitos.categories;

import java.util.List;

import org.springframework.stereotype.Service;

import com.riquitos.AbstractCrudService;

@Service
public class CategoryService extends AbstractCrudService<Category, Long>{
	private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository repository) {
        super(repository); 
        this.categoryRepository = repository;
    }

    @Override
    public List<Category> findAll(String filterText) {
        //if (filterText == null || filterText.isEmpty()) {
            return repository.findAll();
        /*} else {
            return priceListRepository.searchByNameContainingIgnoreCase(filterText);
        }*/
    }
}
