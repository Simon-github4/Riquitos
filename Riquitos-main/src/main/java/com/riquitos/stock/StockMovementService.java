package com.riquitos.stock;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.AbstractCrudService;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;
import com.riquitos.stock.StockMovement.StockMovementType;


@Service
public class StockMovementService extends AbstractCrudService<StockMovement, Long> {

    private final StockMovementRepository stockMovementRepository;
    private final RawMaterialService rawMaterialService;

    public StockMovementService(
            StockMovementRepository repository,
            RawMaterialService rawMaterialService) {
        super(repository);
        this.stockMovementRepository = repository;
        this.rawMaterialService = rawMaterialService;
    }

    @Override
    public List<StockMovement> findAll(String filterText) {
        //if (filterText == null || filterText.isEmpty()) 
            return repository.findAll(Sort.by(Sort.Direction.DESC, "movementDateTime"));
        /*String filter = filterText.toLowerCase();
        return repository.findAll(Sort.by(Sort.Direction.DESC, "movementDateTime")).stream()
            .filter(movement -> 
                (movement.getRawMaterial() != null && 
                 movement.getRawMaterial().getName() != null &&
                 movement.getRawMaterial().getName().toLowerCase().contains(filter)) ||
                (movement.getObservations() != null &&
                 movement.getObservations().toLowerCase().contains(filter)))
            .toList();*/
    }

    @Override
    @Transactional
    public StockMovement save(StockMovement movement) {
        validateMovement(movement);
        updateRawMaterialStock(movement);
        
        return super.save(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findByRawMaterialId(Long rawMaterialId) {
        return stockMovementRepository.findByRawMaterialId(rawMaterialId);
    }
    @Transactional(readOnly = true)
    public List<StockMovement> findByProductionBatchId(Long batchId) {
        return stockMovementRepository.findByProductionBatchId(batchId);
    }
    @Transactional(readOnly = true)
    public List<StockMovement> findByType(StockMovementType type) {
        return stockMovementRepository.findByType(type);
    }

    // Calcula el stock teórico basado en movimientos (para validación)
    public BigDecimal calcularStockTeorico(RawMaterial rawMaterial) {
        List<StockMovement> movimientos = findByRawMaterialId(rawMaterial.getId());
        
        BigDecimal stockTeorico = BigDecimal.ZERO;
        for (StockMovement mov : movimientos) {
            if (mov.getType() == StockMovementType.INGRESO || 
                mov.getType() == StockMovementType.AJUSTE) {
                stockTeorico = stockTeorico.add(mov.getQuantity());
            } else if (mov.getType() == StockMovementType.EGRESO) {
                stockTeorico = stockTeorico.subtract(mov.getQuantity());
            }
        }
        
        return stockTeorico;
    }

    // ===== MÉTODOS PRIVADOS =====
    private void validateMovement(StockMovement movement) {
        if (movement.getType() == null) {
            throw new IllegalArgumentException("Tipo de movimiento requerido");
        }
        
        if (movement.getRawMaterial() == null) {
            throw new IllegalArgumentException("Materia prima requerida");
        }
        
        // Validación específica para egresos
        if (movement.getType() == StockMovementType.EGRESO) {
            if (movement.getProductionBatch() == null) {
                throw new IllegalArgumentException("ProductionBatch requerido para egresos");
            }
        }
    }

    private void updateRawMaterialStock(StockMovement movement) {
    	RawMaterial materiaPrima = rawMaterialService.findById(movement.getRawMaterial().getId()).orElseThrow(() -> 
			new IllegalArgumentException("Materia prima no encontrada con ID: " + movement.getRawMaterial().getId()));
		
    	BigDecimal quantity = movement.getQuantity();
    	BigDecimal nuevoStock;
    	if(movement.getType() == StockMovementType.EGRESO)
    		nuevoStock = materiaPrima.getCurrentStock().subtract(quantity);
    	else
    		nuevoStock = materiaPrima.getCurrentStock().add(quantity); // ingreo o ajuste(+,-)
        
    	materiaPrima.setCurrentStock(nuevoStock);
        rawMaterialService.save(materiaPrima);
    }
}