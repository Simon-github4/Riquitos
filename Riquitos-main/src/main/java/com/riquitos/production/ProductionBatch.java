package com.riquitos.production;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.riquitos.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime productionDate;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal unitiesProduced;
    private BigDecimal bagsOrBoxProduced;

    public ProductionBatch() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDateTime productionDate) { this.productionDate = productionDate; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public BigDecimal getUnitiesProduced() { return unitiesProduced; }
    public void setUnitiesProduced(BigDecimal quantityProduced) { this.unitiesProduced = quantityProduced; }
	public BigDecimal getBagsOrBoxProduced() {
		return bagsOrBoxProduced;
	}
	public void setBagsOrBoxProduced(BigDecimal bagsOrBoxProduced) {
		this.bagsOrBoxProduced = bagsOrBoxProduced;
	}
}
