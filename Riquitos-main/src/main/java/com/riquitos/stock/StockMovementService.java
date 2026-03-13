package com.riquitos.stock;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.AbstractCrudService;
import com.riquitos.production.ProductionBatch;
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
        return repository.findAll(Sort.by(Sort.Direction.DESC, "movementDateTime"));
    }

    @Override
    @Transactional
    public StockMovement save(StockMovement movement) {
        validateMovement(movement);
        updateRawMaterialStock(movement);
        
        return super.save(movement);
    }

    @Override
    @Transactional
    public void delete(StockMovement movement) {
        RawMaterial materiaPrima = movement.getRawMaterial();

        super.delete(movement);
        // Si no haces esto, el cálculo siguiente podría incluir todavía el registro borrado
        repository.flush(); 

        BigDecimal stockReal = calcularStockTeorico(materiaPrima);

        materiaPrima.setCurrentStock(stockReal);
        rawMaterialService.save(materiaPrima);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
		delete(repository.findById(id).orElseThrow(() -> 
			new IllegalArgumentException("Movimiento de stock no encontrado con ID: " + id)));
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

    @Transactional
    public StockMovement saveIngreso(RawMaterial rawMaterial, BigDecimal quantity, String observations) {
        StockMovement movement = StockMovementFactory.createIngreso(rawMaterial, quantity, observations);
        return save(movement);
    }

    @Transactional
	public StockMovement saveEgreso(RawMaterial materiaPrima, BigDecimal quantity, ProductionBatch batch, String observations) {
		StockMovement movement = StockMovementFactory.createEgreso(materiaPrima, quantity, batch, observations);
        return save(movement);
	}
	
    @Transactional
    public StockMovement saveAjuste(RawMaterial rawMaterial, BigDecimal quantity, String observations) {
        StockMovement movement = StockMovementFactory.createAjuste(rawMaterial, quantity, observations);
        return save(movement);
    }
    
    private void validateMovement(StockMovement movement) {
        if (movement.getType() == null) {
            throw new IllegalArgumentException("Tipo de movimiento requerido");
        }
        
        if (movement.getRawMaterial() == null) {
            throw new IllegalArgumentException("Materia prima requerida");
        }
        
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

    
}