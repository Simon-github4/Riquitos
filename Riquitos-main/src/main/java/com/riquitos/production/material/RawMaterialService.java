package com.riquitos.production.material;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.riquitos.AbstractCrudService;

@Service
public class RawMaterialService extends AbstractCrudService<RawMaterial, Long> {

    private final RawMaterialRepository repository;

    public RawMaterialService(RawMaterialRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public RawMaterial save(RawMaterial entity) {
		if(entity.getId() == null)
			entity.setCurrentStock(BigDecimal.ZERO);
		return
		super.save(entity);
	}
    @Override
    public List<RawMaterial> findAll(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return repository.findAll();
        } else {
            // Asumiendo que quieres filtrar por nombre. 
            // Si no tienes un método custom en el repo, puedes usar findAll() y filtrar con stream 
            // o crear findByNameContainingIgnoreCase en el repositorio.
            // Por ahora usamos stream para no obligarte a cambiar el repo inmediatamente:
            return repository.findAll().stream()
                .filter(rm -> rm.getName().toLowerCase().contains(filterText.toLowerCase()))
                .toList();
        }
    }
    
    public List<RawMaterial> findAll() {
    	return repository.findAll();      
    }
}