package com.riquitos.production;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.riquitos.AbstractCrudService;
import com.riquitos.product.Product;
import com.riquitos.product.ProductIngredient;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;

import jakarta.transaction.Transactional;

@Service
public class ProductionBatchService extends AbstractCrudService<ProductionBatch, Long> {

    private final ProductionBatchRepository batchRepository;
    private final RawMaterialService rawMaterialService;

    public ProductionBatchService(ProductionBatchRepository repository, RawMaterialService rawMaterialService) {
        super(repository);
        this.batchRepository = repository;
        this.rawMaterialService = rawMaterialService;
    }

    @Override
    public List<ProductionBatch> findAll(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return repository.findAll(Sort.by(Sort.Direction.DESC, "productionDate"));
        }
        
        String filter = filterText.toLowerCase();
        return repository.findAll(Sort.by(Sort.Direction.DESC, "productionDate")).stream()
            .filter(batch -> 
                (batch.getProduct() != null && 
                 batch.getProduct().getDescription() != null &&
                 batch.getProduct().getDescription().toLowerCase().contains(filter)))
            .toList();
    }

    public List<ProductionBatch> findByProductId(Long productId) {
        return batchRepository.findByProductId(productId);
    }
    
    @Transactional // IMPORTANTE: Si algo falla, se hace rollback de todo
    public void registrarProduccion(Product producto, BigDecimal cantidadProducida) {
        
        // 1. Crear y guardar la tanda (Histórico)
        ProductionBatch batch = new ProductionBatch();
        batch.setProduct(producto);
        batch.setQuantityProduced(cantidadProducida);
        batch.setProductionDate(LocalDateTime.now());
        
        batchRepository.save(batch);

        // 2. Descontar materias primas (Receta)
        if (producto.getRecipe() != null) {
            for (ProductIngredient ingrediente : producto.getRecipe()) {
                
                BigDecimal consumoTotal = cantidadProducida.multiply(ingrediente.getQuantityRequired());

                RawMaterial materiaPrima = ingrediente.getRawMaterial();
                
                if (materiaPrima.getCurrentStock().compareTo(consumoTotal) < 0) {
                   // throw new RuntimeException("Stock insuficiente de: " + materiaPrima.getName());
                   // O simplemente dejar que vaya a negativo para alertar compras
                }

                BigDecimal nuevoStock = materiaPrima.getCurrentStock().subtract(consumoTotal);
                materiaPrima.setCurrentStock(nuevoStock);

                rawMaterialService.save(materiaPrima);
            }
        }
    }
}