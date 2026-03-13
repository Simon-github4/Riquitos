package com.riquitos.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.riquitos.production.ProductionBatch;
import com.riquitos.production.material.RawMaterial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "stock_movements")
@Data
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // COMUNES
    @Column(nullable = false)
    private LocalDateTime movementDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type; // INGRESO, EGRESO, AJUSTE

    @Column(length = 500)
    private String observations;
    
    @ManyToOne
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;
    
    @Column(nullable = false)
    private BigDecimal quantity;

    
    // PARA EGRESOS (null si es ingreso)
    @ManyToOne
    @JoinColumn(name = "production_batch_id")
    private ProductionBatch productionBatch;

    /* PARA INGRESOS (null si es egreso)
    private BigDecimal unitPrice;
    private BigDecimal totalCost;
    private String lotNumber;*/
    
    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", movementDateTime=" + movementDateTime +
                ", type=" + type +
                ", rawMaterialId=" + (rawMaterial != null ? rawMaterial.getId() : null) +
                ", quantity=" + quantity +
                ", productionBatchId=" + (productionBatch != null ? productionBatch.getId() : null) +
                ", observations='" + observations + '\'' +
                '}';
    }
    
    public enum StockMovementType {
        INGRESO,    // Compra a proveedor
        EGRESO,     // Consumo en producción
        AJUSTE      // Corrección de inventario
    }
    
    public LocalDateTime getMovementDateTime() {
        return movementDateTime;
    }
    
    public void setMovementDateTime(LocalDateTime movementDateTime) {
        this.movementDateTime = movementDateTime;
    }
    
    public StockMovementType getType() {
        return type;
    }
    
    public void setType(StockMovementType type) {
        this.type = type;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public RawMaterial getRawMaterial() {
        return rawMaterial;
    }
    
    public void setRawMaterial(RawMaterial rawMaterial) {
        this.rawMaterial = rawMaterial;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public ProductionBatch getProductionBatch() {
        return productionBatch;
    }
    
    public void setProductionBatch(ProductionBatch productionBatch) {
        this.productionBatch = productionBatch;
    }
}