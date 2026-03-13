package com.riquitos.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.riquitos.production.ProductionBatch;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.stock.StockMovement.StockMovementType;

public class StockMovementFactory {

    private StockMovementFactory() {
    }

    public static StockMovement createIngreso(RawMaterial rawMaterial, BigDecimal quantity, String observations) {
        validateRawMaterial(rawMaterial);
        validateQuantity(quantity);
        
        StockMovement movement = new StockMovement();
        movement.setType(StockMovementType.INGRESO);
        movement.setRawMaterial(rawMaterial);
        movement.setQuantity(quantity);
        movement.setMovementDateTime(LocalDateTime.now());
        movement.setObservations(observations);
        return movement;
    }

    public static StockMovement createEgreso(RawMaterial rawMaterial, BigDecimal quantity, 
            ProductionBatch productionBatch, String observations) {
        validateRawMaterial(rawMaterial);
        validateQuantity(quantity);
        
        if (productionBatch == null) {
            throw new IllegalArgumentException("ProductionBatch requerido para egresos");
        }

        StockMovement movement = new StockMovement();
        movement.setType(StockMovementType.EGRESO);
        movement.setRawMaterial(rawMaterial);
        movement.setQuantity(quantity);
        movement.setProductionBatch(productionBatch);
        movement.setMovementDateTime(LocalDateTime.now());
        movement.setObservations(observations);
        return movement;
    }

    public static StockMovement createAjuste(RawMaterial rawMaterial, BigDecimal quantity, String observations) {
        validateRawMaterial(rawMaterial);
        validateQuantity(quantity);

        StockMovement movement = new StockMovement();
        movement.setType(StockMovementType.AJUSTE);
        movement.setRawMaterial(rawMaterial);
        movement.setQuantity(quantity);
        movement.setMovementDateTime(LocalDateTime.now());
        movement.setObservations(observations);
        return movement;
    }

    private static void validateRawMaterial(RawMaterial rawMaterial) {
        if (rawMaterial == null) {
            throw new IllegalArgumentException("Materia prima requerida");
        }
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null /*|| quantity.compareTo(BigDecimal.ZERO) <= 0*/) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a cero");
        }
    }
}
