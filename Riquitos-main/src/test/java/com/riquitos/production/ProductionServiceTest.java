package com.riquitos.production;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.riquitos.product.Product;
import com.riquitos.product.ProductIngredient;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialRepository;
import com.riquitos.production.material.RawMaterialService;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private RawMaterialService rawMaterialService;
    
    @Mock
    private RawMaterialRepository rawMaterialRepo;

    @InjectMocks
    private ProductionBatchService productionBatchService;

    private Product testProduct;
    private RawMaterial testRawMaterial;
    private ProductIngredient testIngredient;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setDescription("Pan");
        
        testRawMaterial = new RawMaterial();
        testRawMaterial.setId(1L);
        testRawMaterial.setName("Harina");
        testRawMaterial.setCurrentStock(new BigDecimal("100"));
        
        testIngredient = new ProductIngredient();
        testIngredient.setProduct(testProduct);
        testIngredient.setRawMaterial(testRawMaterial);
        testIngredient.setQuantityRequired(new BigDecimal("0.5"));
        
        testProduct.setRecipe(Arrays.asList(testIngredient));
    }

    @Test
    void testRegistrarProduccion_Exito() {
        BigDecimal cantidadProducida = new BigDecimal("10");
        
        ProductionBatch savedBatch = new ProductionBatch();
        savedBatch.setId(1L);
        savedBatch.setProduct(testProduct);
        savedBatch.setQuantityProduced(cantidadProducida);
        savedBatch.setProductionDate(LocalDateTime.now());
        
        when(productionBatchService.save(any(ProductionBatch.class))).thenReturn(savedBatch);
        
        productionBatchService.registrarProduccion(testProduct, cantidadProducida);
        
        verify(productionBatchService).save(argThat(batch -> 
            batch.getProduct().equals(testProduct) &&
            batch.getQuantityProduced().equals(cantidadProducida) &&
            batch.getProductionDate() != null
        ));
        
        verify(rawMaterialService).save(testRawMaterial);
    }

    @Test
    void testRegistrarProduccion_SinReceta() {
        testProduct.setRecipe(null);
        BigDecimal cantidadProducida = new BigDecimal("5");
        
        when(productionBatchService.save(any(ProductionBatch.class))).thenReturn(new ProductionBatch());
        
        productionBatchService.registrarProduccion(testProduct, cantidadProducida);
        
        verify(productionBatchService).save(any(ProductionBatch.class));
        verify(rawMaterialService, never()).save(any());
    }

    @Test
    void testRegistrarProduccion_StockInsuficiente_NoLanzaExcepcion() {
        testRawMaterial.setCurrentStock(new BigDecimal("2"));
        BigDecimal cantidadProducida = new BigDecimal("10");
        
        when(productionBatchService.save(any(ProductionBatch.class))).thenReturn(new ProductionBatch());
        
        assertDoesNotThrow(() -> productionBatchService.registrarProduccion(testProduct, cantidadProducida));
        
        verify(rawMaterialService).save(argThat(material -> 
            material.getCurrentStock().compareTo(new BigDecimal("0")) < 0
        ));
    }

    @Test
    void testRegistrarProduccion_ConMultiplesIngredientes() {
        RawMaterial segundaMateriaPrima = new RawMaterial();
        segundaMateriaPrima.setId(2L);
        segundaMateriaPrima.setName("Levadura");
        segundaMateriaPrima.setCurrentStock(new BigDecimal("50"));
        
        ProductIngredient segundoIngrediente = new ProductIngredient();
        segundoIngrediente.setProduct(testProduct);
        segundoIngrediente.setRawMaterial(segundaMateriaPrima);
        segundoIngrediente.setQuantityRequired(new BigDecimal("0.1"));
        
        testProduct.setRecipe(Arrays.asList(testIngredient, segundoIngrediente));
        
        BigDecimal cantidadProducida = new BigDecimal("10");
        
        when(productionBatchService.save(any(ProductionBatch.class))).thenReturn(new ProductionBatch());
        
        productionBatchService.registrarProduccion(testProduct, cantidadProducida);
        
        verify(rawMaterialService, times(2)).save(any(RawMaterial.class));
    }

    @Test
    void testRegistrarProduccion_ValoresCero() {
        BigDecimal cantidadCero = BigDecimal.ZERO;
        
        when(productionBatchService.save(any(ProductionBatch.class))).thenReturn(new ProductionBatch());
        
        productionBatchService.registrarProduccion(testProduct, cantidadCero);
        
        verify(productionBatchService).save(any(ProductionBatch.class));
        verify(rawMaterialService).save(argThat(material -> 
            material.getCurrentStock().equals(testRawMaterial.getCurrentStock())
        ));
    }
}