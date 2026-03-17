package com.riquitos.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {

    List<ProductionBatch> findByProductId(Long productId);
 
    @Query("select b from ProductionBatch b " +
            "where (lower(b.product.description) like lower(concat('%', :filterText, '%')) or :filterText is null) " +
            // Agregamos cast(:dateFrom as date) para dar la pista de tipo a Postgres
            "and (cast(b.productionDate as LocalDate) >= :dateFrom or cast(:dateFrom as date) is null) " + 
            "and (cast(b.productionDate as LocalDate) <= :dateTo or cast(:dateTo as date) is null)" +
            "ORDER BY productionDate DESC")
     List<ProductionBatch> search(@Param("filterText") String filterText, 
                                  @Param("dateFrom") LocalDate dateFrom, 
                                  @Param("dateTo") LocalDate dateTo);
    
    //Contar producción desde el inicio del día de hoy
    long countByProductionDateAfter(LocalDateTime date);
    
    @Query("SELECT p FROM ProductionBatch p " +
            "WHERE p.product.id = :productId " +
            "AND p.productionDate BETWEEN :start AND :end " +
            "AND p.bagsOrBoxProduced = :units")
     List<ProductionBatch> findByProductIdAndProductionDateBetweenAndBagsOrBoxProduced(
         @Param("productId") Long productId, 
         @Param("start") LocalDateTime start, 
         @Param("end") LocalDateTime end, 
         @Param("units") BigDecimal units
     );
}
