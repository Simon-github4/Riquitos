package com.riquitos.stock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.riquitos.stock.StockMovement.StockMovementType;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
	List<StockMovement> findByRawMaterialId(Long rawMaterialId);
    
    /**
     * Encuentra todos los movimientos asociados a un batch de producción
     */
    List<StockMovement> findByProductionBatchId(Long productionBatchId);
    
    /**
     * Encuentra movimientos por tipo (INGRESO, EGRESO, AJUSTE)
     */
    List<StockMovement> findByType(StockMovementType type);
    
    /**
     * Encuentra movimientos por materia prima y tipo
     */
    List<StockMovement> findByRawMaterialIdAndType(Long rawMaterialId, StockMovementType type);
    
}
