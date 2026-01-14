package com.riquitos.customers;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.AbstractCrudService;

@Service
public class CustomerService extends AbstractCrudService<Customer, Long> {

	private CustomerRepository customerRepository;
	
    public CustomerService(CustomerRepository repository) {
        super(repository); 
        this.customerRepository = repository;
    }

    @Override
    @Transactional(readOnly=true)
    public List<Customer> findAll(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return findAll();
        } else {
        	return customerRepository.findAll((root, query, cb) -> {
                String pattern = "%" + filterText.toLowerCase() + "%";
                
                return cb.or(
                    // Busca en el nombre (ignorando mayúsculas/minúsculas)
                    cb.like(cb.lower(root.get("name")), pattern)
                    
                    // O busca en el email (si lo tienes mapeado)
                    //cb.like(cb.lower(root.get("email")), pattern),
                );
            });
        }
    }
    
    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        // Aquí podrías usar un método custom del repo o Specifications
        return customerRepository.findOne((root, query, cb) -> 
            cb.equal(root.get("email"), email)
        ).orElse(null);
    }
}