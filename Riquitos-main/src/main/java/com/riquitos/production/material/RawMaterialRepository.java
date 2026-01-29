package com.riquitos.production.material;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    
    // Buscar insumo por nombre exacto (útil para validaciones antes de crear)
    Optional<RawMaterial> findByName(String name);
    
    // Contar cuántos insumos tienen stock menor a un límite (ej: 10)
    long countByCurrentStockLessThan(BigDecimal limit);

    // Buscar los insumos con stock bajo para mostrarlos en la lista
    List<RawMaterial> findByCurrentStockLessThan(BigDecimal limit);
}
