package com.riquitos.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.production.ProductionBatch;
import com.riquitos.production.ProductionBatchRepository;
import com.riquitos.product.Product;
import com.riquitos.product.ProductIngredient;
import com.riquitos.production.material.RawMaterial;

@SpringBootTest
@Transactional
class ProductionIntegrationTest {

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private ProductionBatchRepository batchRepository;

    @Test
    void testGuardarYRecuperarProductionBatch() {
        Product product = new Product();
        product.setDescription("Pan de Molde");
        product.setCostPrice(new BigDecimal("2.50"));
        entityManager.persist(product);

        ProductionBatch batch = new ProductionBatch();
        batch.setProduct(product);
        batch.setUnitiesProduced(new BigDecimal("100"));
        batch.setProductionDate(LocalDateTime.now());

        entityManager.persist(batch);
        entityManager.flush();
        ProductionBatch guardado = batch;
        
        ProductionBatch encontrado = batchRepository.findById(guardado.getId()).orElse(null);
        
        assertNotNull(encontrado);
        assertEquals(product.getId(), encontrado.getProduct().getId());
        assertEquals(new BigDecimal("100"), encontrado.getUnitiesProduced());
        assertNotNull(encontrado.getProductionDate());
    }

    @Test
    void testRelacionProductIngredientConRawMaterial() {
        RawMaterial harina = new RawMaterial();
        harina.setName("Harina");
        harina.setUnit("KG");
        harina.setCurrentStock(new BigDecimal("100"));
        entityManager.persist(harina);

        Product pan = new Product();
        pan.setDescription("Pan Blanco");
        pan.setCostPrice(new BigDecimal("1.50"));
        entityManager.persist(pan);
        entityManager.flush();

        ProductIngredient ingrediente = new ProductIngredient();
        ingrediente.setProduct(pan);
        ingrediente.setRawMaterial(harina);
        ingrediente.setQuantityRequired(new BigDecimal("0.5"));
        entityManager.persist(ingrediente);
        entityManager.flush();

        ProductIngredient encontrado = entityManager.find(ProductIngredient.class, ingrediente.getId());
        
        assertNotNull(encontrado);
        assertEquals(pan.getId(), encontrado.getProduct().getId());
        assertEquals(harina.getId(), encontrado.getRawMaterial().getId());
        assertEquals(new BigDecimal("0.5"), encontrado.getQuantityRequired());
    }

    @Test
    void testProduccionComplejaConMultiplesIngredientes() {
        RawMaterial harina = new RawMaterial();
        harina.setName("Harina");
        harina.setUnit("KG");
        harina.setCurrentStock(new BigDecimal("100"));
        entityManager.persist(harina);

        RawMaterial levadura = new RawMaterial();
        levadura.setName("Levadura");
        levadura.setUnit("G");
        levadura.setCurrentStock(new BigDecimal("500"));
        entityManager.persist(levadura);

        RawMaterial agua = new RawMaterial();
        agua.setName("Agua");
        agua.setUnit("L");
        agua.setCurrentStock(new BigDecimal("50"));
        entityManager.persist(agua);

        Product pan = new Product();
        pan.setDescription("Pan Integral");
        pan.setCostPrice(new BigDecimal("3.00"));
        entityManager.persist(pan);

        ProductIngredient ing1 = new ProductIngredient();
        ing1.setProduct(pan);
        ing1.setRawMaterial(harina);
        ing1.setQuantityRequired(new BigDecimal("0.5"));
        entityManager.persist(ing1);

        ProductIngredient ing2 = new ProductIngredient();
        ing2.setProduct(pan);
        ing2.setRawMaterial(levadura);
        ing2.setQuantityRequired(new BigDecimal("5"));
        entityManager.persist(ing2);

        ProductIngredient ing3 = new ProductIngredient();
        ing3.setProduct(pan);
        ing3.setRawMaterial(agua);
        ing3.setQuantityRequired(new BigDecimal("0.3"));
        entityManager.persist(ing3);

        ProductionBatch batch = new ProductionBatch();
        batch.setProduct(pan);
        batch.setUnitiesProduced(new BigDecimal("20"));
        batch.setProductionDate(LocalDateTime.now());
        entityManager.persist(batch);

        ProductionBatch encontrado = entityManager.find(ProductionBatch.class, batch.getId());
        
        assertNotNull(encontrado);
        assertEquals(pan.getId(), encontrado.getProduct().getId());
        assertEquals(new BigDecimal("20"), encontrado.getUnitiesProduced());
    }

    @Test
    void testStockActualizadoDespuesDeProduccion() {
        BigDecimal stockInicial = new BigDecimal("100");
        
        RawMaterial harina = new RawMaterial();
        harina.setName("Harina");
        harina.setUnit("KG");
        harina.setCurrentStock(new BigDecimal("100"));
        entityManager.persist(harina);
        entityManager.flush();

        Product pan = new Product();
        pan.setDescription("Pan Simple");
        pan.setCostPrice(new BigDecimal("2.00"));
        entityManager.persist(pan);

        ProductIngredient ingrediente = new ProductIngredient();
        ingrediente.setProduct(pan);
        ingrediente.setRawMaterial(harina);
        ingrediente.setQuantityRequired(new BigDecimal("0.5"));
        entityManager.persist(ingrediente);

        BigDecimal cantidadProducida = new BigDecimal("10");
        BigDecimal consumoEsperado = cantidadProducida.multiply(ingrediente.getQuantityRequired());
        BigDecimal stockFinalEsperado = stockInicial.subtract(consumoEsperado);

        ProductionBatch batch = new ProductionBatch();
        batch.setProduct(pan);
        batch.setUnitiesProduced(cantidadProducida);
        batch.setProductionDate(LocalDateTime.now());
        entityManager.persist(batch);

        RawMaterial materialActualizado = entityManager.find(RawMaterial.class, harina.getId());
        
        assertEquals(stockFinalEsperado, materialActualizado.getCurrentStock());
    }

    @Test
    void testFindByProductId() {
        Product pan = new Product();
        pan.setDescription("Pan Variado");
        pan.setCostPrice(new BigDecimal("2.50"));
        entityManager.persist(pan);

        Product pastel = new Product();
        pastel.setDescription("Pastel");
        pastel.setCostPrice(new BigDecimal("5.00"));
        entityManager.persist(pastel);

        ProductionBatch batch1 = new ProductionBatch();
        batch1.setProduct(pan);
        batch1.setUnitiesProduced(new BigDecimal("50"));
        batch1.setProductionDate(LocalDateTime.now());
        entityManager.persist(batch1);

        ProductionBatch batch2 = new ProductionBatch();
        batch2.setProduct(pan);
        batch2.setUnitiesProduced(new BigDecimal("30"));
        batch2.setProductionDate(LocalDateTime.now().plusHours(1));
        entityManager.persist(batch2);

        ProductionBatch batch3 = new ProductionBatch();
        batch3.setProduct(pastel);
        batch3.setUnitiesProduced(new BigDecimal("15"));
        batch3.setProductionDate(LocalDateTime.now().plusHours(2));
        entityManager.persist(batch3);

        List<ProductionBatch> batchesDelPan = batchRepository.findAll().stream()
            .filter(batch -> batch.getProduct().getId().equals(pan.getId()))
            .toList();

        assertEquals(2, batchesDelPan.size());
        assertTrue(batchesDelPan.stream().allMatch(batch -> 
            batch.getProduct().getId().equals(pan.getId())
        ));
    }
}