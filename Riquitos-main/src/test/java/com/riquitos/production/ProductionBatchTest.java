package com.riquitos.production;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.riquitos.product.Product;

class ProductionBatchTest {

    private ProductionBatch batch;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        batch = new ProductionBatch();
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setDescription("Pan Integral");
    }

    @Test
    void testConstructorVacio() {
        ProductionBatch nuevoBatch = new ProductionBatch();
        
        assertNull(nuevoBatch.getId());
        assertNull(nuevoBatch.getProductionDate());
        assertNull(nuevoBatch.getProduct());
        assertNull(nuevoBatch.getUnitiesProduced());
    }

    @Test
    void testSettersYGetters() {
        Long expectedId = 123L;
        LocalDateTime expectedDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        BigDecimal expectedQuantity = new BigDecimal("50.5");
        
        batch.setId(expectedId);
        batch.setProductionDate(expectedDate);
        batch.setProduct(testProduct);
        batch.setUnitiesProduced(expectedQuantity);
        
        assertEquals(expectedId, batch.getId());
        assertEquals(expectedDate, batch.getProductionDate());
        assertEquals(testProduct, batch.getProduct());
        assertEquals(expectedQuantity, batch.getUnitiesProduced());
    }

    @Test
    void testProduccionValida() {
        batch.setProduct(testProduct);
        batch.setUnitiesProduced(new BigDecimal("100"));
        batch.setProductionDate(LocalDateTime.now());
        
        assertNotNull(batch.getProduct());
        assertNotNull(batch.getUnitiesProduced());
        assertTrue(batch.getUnitiesProduced().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCantidadCero() {
        batch.setUnitiesProduced(BigDecimal.ZERO);
        
        assertEquals(BigDecimal.ZERO, batch.getUnitiesProduced());
    }

    @Test
    void testCantidadNegativa() {
        BigDecimal cantidadNegativa = new BigDecimal("-10");
        batch.setUnitiesProduced(cantidadNegativa);
        
        assertEquals(cantidadNegativa, batch.getUnitiesProduced());
    }

    @Test
    void testProduccionSinProducto() {
        batch.setUnitiesProduced(new BigDecimal("25"));
        batch.setProductionDate(LocalDateTime.now());
        
        assertNull(batch.getProduct());
        assertNotNull(batch.getUnitiesProduced());
    }

    @Test
    void testProduccionSinCantidad() {
        batch.setProduct(testProduct);
        batch.setProductionDate(LocalDateTime.now());
        
        assertNotNull(batch.getProduct());
        assertNull(batch.getUnitiesProduced());
    }

    @Test
    void testProduccionSinFecha() {
        batch.setProduct(testProduct);
        batch.setUnitiesProduced(new BigDecimal("75"));
        
        assertNotNull(batch.getProduct());
        assertNotNull(batch.getUnitiesProduced());
        assertNull(batch.getProductionDate());
    }

    @Test
    void testCambiosEnProductoReferencia() {
        batch.setProduct(testProduct);
        
        Product mismoProducto = batch.getProduct();
        mismoProducto.setDescription("Pan Modificado");
        
        assertEquals("Pan Modificado", batch.getProduct().getDescription());
    }

    @Test
    void testDecimalPrecision() {
        BigDecimal cantidadPrecisa = new BigDecimal("123.456789");
        batch.setUnitiesProduced(cantidadPrecisa);
        
        assertEquals(cantidadPrecisa, batch.getUnitiesProduced());
        assertEquals(0, cantidadPrecisa.compareTo(batch.getUnitiesProduced()));
    }
}