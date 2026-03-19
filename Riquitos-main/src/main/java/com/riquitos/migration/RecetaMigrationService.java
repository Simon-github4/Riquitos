package com.riquitos.migration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.product.Product;
import com.riquitos.production.ProductionBatch;
import com.riquitos.production.ProductionBatchRepository;
import com.riquitos.production.ProductionBatchService;

@Service
public class RecetaMigrationService {

    private static final Logger log = LoggerFactory.getLogger(RecetaMigrationService.class);

    @Autowired
    private ProductionBatchService productionBatchService;

    @Transactional
    public int recalcularTodasLasRecetas() {
        List<ProductionBatch> batches = productionBatchService.findAll();
        
        log.info("Iniciando migracion de recetas. Total de batches: {}", batches.size());
        
        int actualizados = 0;
        
        for (ProductionBatch batch : batches) {
            try {
                Product producto = batch.getProduct();
                
                if (producto == null) {
                    log.warn("Batch {} sin producto, saltando...", batch.getId());
                    continue;
                }
                
                /*if (producto.getRecipe() == null || producto.getRecipe().isEmpty()) {
                    log.info("Batch {} - Producto {} sin receta, saltando...", batch.getId(), producto.getDescription());
                    continue;
                }*/
                
                log.info("Procesando batch {} - Producto: {} - Fecha: {}", 
                    batch.getId(), 
                    producto.getDescription(), 
                    batch.getProductionDate());
                
                // Anular los movimientos de stock existentes
                productionBatchService.anularProduccion(batch);
                
                // Volver a registrar con la receta actual
                productionBatchService.registrarProduccion(
                    producto, 
                    batch.getBagsOrBoxProduced(), 
                    batch.getProductionDate()
                );
                
                actualizados++;
                log.info("Batch {} actualizado correctamente", batch.getId());
                
            } catch (Exception e) {
                log.error("Error al procesar batch {}: {}", batch.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Migracion completada. Batches actualizados: {}/{}", actualizados, batches.size());
        
        return actualizados;
    }
}
