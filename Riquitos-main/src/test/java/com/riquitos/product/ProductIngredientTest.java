package com.riquitos.product;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.riquitos.production.material.RawMaterial;

class ProductIngredientTest {

    private ProductIngredient ingredient;
    private Product testProduct;
    private RawMaterial testRawMaterial;

    @BeforeEach
    void setUp() {
        ingredient = new ProductIngredient();
        
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setDescription("Pan Blanco");
        
        testRawMaterial = new RawMaterial();
        testRawMaterial.setId(1L);
        testRawMaterial.setName("Harina");
    }

    @Test
    void testConstructorVacio() {
        ProductIngredient nuevoIngredient = new ProductIngredient();
        
        assertNull(nuevoIngredient.getId());
        assertNull(nuevoIngredient.getProduct());
        assertNull(nuevoIngredient.getRawMaterial());
        assertNull(nuevoIngredient.getQuantityRequired());
    }

    @Test
    void testSettersYGetters() {
        Long expectedId = 456L;
        BigDecimal expectedQuantity = new BigDecimal("2.5");
        
        ingredient.setId(expectedId);
        ingredient.setProduct(testProduct);
        ingredient.setRawMaterial(testRawMaterial);
        ingredient.setQuantityRequired(expectedQuantity);
        
        assertEquals(expectedId, ingredient.getId());
        assertEquals(testProduct, ingredient.getProduct());
        assertEquals(testRawMaterial, ingredient.getRawMaterial());
        assertEquals(expectedQuantity, ingredient.getQuantityRequired());
    }

    @Test
    void testIngredienteCompleto() {
        ingredient.setProduct(testProduct);
        ingredient.setRawMaterial(testRawMaterial);
        ingredient.setQuantityRequired(new BigDecimal("1.0"));
        
        assertNotNull(ingredient.getProduct());
        assertNotNull(ingredient.getRawMaterial());
        assertNotNull(ingredient.getQuantityRequired());
        assertTrue(ingredient.getQuantityRequired().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCantidadCero() {
        ingredient.setQuantityRequired(BigDecimal.ZERO);
        
        assertEquals(BigDecimal.ZERO, ingredient.getQuantityRequired());
    }

    @Test
    void testCantidadNegativa() {
        BigDecimal cantidadNegativa = new BigDecimal("-0.5");
        ingredient.setQuantityRequired(cantidadNegativa);
        
        assertEquals(cantidadNegativa, ingredient.getQuantityRequired());
    }

    @Test
    void testCantidadMuyPequena() {
        BigDecimal cantidadPequena = new BigDecimal("0.001");
        ingredient.setQuantityRequired(cantidadPequena);
        
        assertEquals(cantidadPequena, ingredient.getQuantityRequired());
    }

    @Test
    void testSinProducto() {
        ingredient.setRawMaterial(testRawMaterial);
        ingredient.setQuantityRequired(new BigDecimal("3"));
        
        assertNull(ingredient.getProduct());
        assertNotNull(ingredient.getRawMaterial());
        assertNotNull(ingredient.getQuantityRequired());
    }

    @Test
    void testSinMateriaPrima() {
        ingredient.setProduct(testProduct);
        ingredient.setQuantityRequired(new BigDecimal("2"));
        
        assertNotNull(ingredient.getProduct());
        assertNull(ingredient.getRawMaterial());
        assertNotNull(ingredient.getQuantityRequired());
    }

    @Test
    void testSinCantidad() {
        ingredient.setProduct(testProduct);
        ingredient.setRawMaterial(testRawMaterial);
        
        assertNotNull(ingredient.getProduct());
        assertNotNull(ingredient.getRawMaterial());
        assertNull(ingredient.getQuantityRequired());
    }

    @Test
    void testPrecisionDecimal() {
        BigDecimal cantidadPrecisa = new BigDecimal("0.333333");
        ingredient.setQuantityRequired(cantidadPrecisa);
        
        assertEquals(cantidadPrecisa, ingredient.getQuantityRequired());
        assertEquals(0, cantidadPrecisa.compareTo(ingredient.getQuantityRequired()));
    }

    @Test
    void testCambiosEnReferencias() {
        ingredient.setProduct(testProduct);
        ingredient.setRawMaterial(testRawMaterial);
        
        Product mismoProducto = ingredient.getProduct();
        mismoProducto.setDescription("Pan Modificado");
        
        RawMaterial mismaMateriaPrima = ingredient.getRawMaterial();
        mismaMateriaPrima.setName("Harina Modificada");
        
        assertEquals("Pan Modificado", ingredient.getProduct().getDescription());
        assertEquals("Harina Modificada", ingredient.getRawMaterial().getName());
    }

    @Test
    void testValoresGrandes() {
        BigDecimal cantidadGrande = new BigDecimal("999999.99");
        ingredient.setQuantityRequired(cantidadGrande);
        
        assertEquals(cantidadGrande, ingredient.getQuantityRequired());
    }
}