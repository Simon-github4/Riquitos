package com.riquitos.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;
    
    @Column(name = "production_unit", nullable = true)
    private int productionUnit;

    @Column(name = "sku")
    private String sku;

    // Using BigDecimal for monetary precision
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductIngredient> recipe;
    
    @Lob
    @Column(name = "image_data")
    private byte[] imageData;
    
    public Product() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

	public String getSku() {return sku;}
	public void setSku(String sku) {this.sku = sku;}
    
	
	public List<ProductIngredient> getRecipe() {return recipe;}
	public void setRecipe(List<ProductIngredient> recipe) {this.recipe = recipe;}
	
	public byte[] getImageData() {return imageData;}
	public void setImageData(byte[] imageData) {this.imageData = imageData;}

	@Override
    public String toString() {
    	return description;
    }
}
