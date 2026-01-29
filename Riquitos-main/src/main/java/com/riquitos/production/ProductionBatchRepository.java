package com.riquitos.production;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {

    List<ProductionBatch> findByProductId(Long productId);
 
    //Contar producción desde el inicio del día de hoy
    long countByProductionDateAfter(LocalDateTime date);
}
