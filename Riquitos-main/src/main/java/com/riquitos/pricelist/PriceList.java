package com.riquitos.pricelist;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.riquitos.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "price_lists")
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String name; // e.g., "Wholesale", "Retail"

    // Margin as a percentage. e.g., 30.00 for 30%
    @Column(name = "margin_percentage", nullable = false, precision = 5, scale = 2)
    @NotNull
    private BigDecimal marginPercentage;

    public PriceList() {}

    public PriceList(String string, double d) {
		this.name = string;
		this.marginPercentage = BigDecimal.valueOf(d);
	}

	// BUSINESS LOGIC: Utility method to calculate price dynamically.
    // @Transient tells JPA to ignore this method for database mapping.
    @Transient 
    public BigDecimal calculateSalePrice(Product product) {
        if (product == null || product.getCostPrice() == null) {
            return BigDecimal.ZERO;
        }

        // Factor = 1 + (margin / 100)
        // e.g., If margin is 30%, factor becomes 1.30
        BigDecimal factor = marginPercentage
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                .add(BigDecimal.ONE);

        // Final Price = Cost * Factor
        return product.getCostPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getMarginPercentage() { return marginPercentage; }
    public void setMarginPercentage(BigDecimal marginPercentage) { 
        this.marginPercentage = marginPercentage; 
    }
}
