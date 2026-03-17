package com.riquitos.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.AbstractCrudService;
import com.riquitos.product.Product;
import com.riquitos.product.ProductIngredient;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.stock.StockMovement;
import com.riquitos.stock.StockMovementFactory;
import com.riquitos.stock.StockMovementService;


@Service
public class ProductionBatchService extends AbstractCrudService<ProductionBatch, Long> {

	private final ProductionBatchRepository batchRepository;
	private final StockMovementService stockMovementService;

	public ProductionBatchService(ProductionBatchRepository repository, StockMovementService stockMovementService) {
		super(repository);
		this.batchRepository = repository;
		this.stockMovementService = stockMovementService;
	}

    @Override
    @Transactional(readOnly = true)
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
    
    @Transactional(readOnly = true)
    public List<ProductionBatch> findAll(String filterText, LocalDate dateFrom, LocalDate dateTo) {
        if ((filterText == null || filterText.isEmpty()) && dateFrom == null && dateTo == null) {
            return batchRepository.findAll(Sort.by(Sort.Direction.DESC, "productionDate"));
        }
        return batchRepository.search(filterText, dateFrom, dateTo);
    }
    
    @Transactional(readOnly = true)
    public List<ProductionBatch> findByProductId(Long productId) {
        return batchRepository.findByProductId(productId);
    }
    
    @Transactional
    public void registrarProduccion(Product producto, BigDecimal cantidadBolsonesProducida) {
        registrarProduccion(producto, cantidadBolsonesProducida, LocalDateTime.now());
    }
    
    @Transactional
    public void registrarProduccion(Product producto, BigDecimal cantidadBolsonesProducida, LocalDateTime productionDateTime) {
        BigDecimal cantidadUnidadesProducidas = cantidadBolsonesProducida.multiply(BigDecimal.valueOf(producto.getUnitiesPerBagOrBox()));
        
        ProductionBatch batch = new ProductionBatch();
        batch.setProduct(producto);
        batch.setBagsOrBoxProduced(cantidadBolsonesProducida);
        batch.setUnitiesProduced(cantidadUnidadesProducidas);
        batch.setProductionDate(productionDateTime);
        
        batchRepository.save(batch);

        if (producto.getRecipe() != null) {
            for (ProductIngredient ingrediente : producto.getRecipe()) {
            	BigDecimal pesoTotalProducidoKg = cantidadUnidadesProducidas.multiply(BigDecimal.valueOf(producto.getNetWeight()))
            																.divide(BigDecimal.valueOf(1000));
                BigDecimal consumoTotal = pesoTotalProducidoKg.multiply(ingrediente.getQuantityRequired());

                RawMaterial materiaPrima = ingrediente.getRawMaterial();
                
                if (materiaPrima.getCurrentStock().compareTo(consumoTotal) < 0) {
                   // throw new RuntimeException("Stock insuficiente de: " + materiaPrima.getName());
                }

                stockMovementService.saveEgreso(
                    materiaPrima,
                    consumoTotal,
                    batch,
                    "Consumo en producción de " + producto.getDescription()
                );
            }
        }
    }
    
    @Transactional // IMPORTANTE: Para que todo ocurra en una sola transacción atómica
    public void anularProduccion(ProductionBatch batch) {
        List<StockMovement> movimientosAsociados = stockMovementService.findByProductionBatchId(batch.getId());

        if (!movimientosAsociados.isEmpty()) {
        	movimientosAsociados.forEach(m-> stockMovementService.delete(m));
        }

        batchRepository.delete(batch);
    }
    
    //Obtiene todos los movimientos de stock asociados a un batch de producción
    @Transactional(readOnly = true)
    public List<StockMovement> obtenerMovimientosDeBatch(Long batchId) {
        return stockMovementService.findByProductionBatchId(batchId);
    }
    
    @Transactional(readOnly = true)
    public boolean existsDuplicado(Long productId, LocalDateTime productionDateTime, BigDecimal cantidadBolsones) {
        LocalDateTime startOfDay = productionDateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = productionDateTime.toLocalDate().atTime(23, 59, 59);
        
        List<ProductionBatch> duplicados = batchRepository.findByProductIdAndProductionDateBetweenAndBagsOrBoxProduced(
            productId, startOfDay, endOfDay, cantidadBolsones
        );
        
        return !duplicados.isEmpty();
    }

}