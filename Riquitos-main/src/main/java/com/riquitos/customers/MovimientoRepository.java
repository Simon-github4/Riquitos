package com.riquitos.customers;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    // Este método es clave para mostrar la ficha "tipo Excel" ordenada correctamente.
    // Ordena por la fecha que eligió el usuario (fecha) y desempata con el ID (id).
    List<Movimiento> findByClienteIdOrderByFechaDescIdDesc(Long customerId);
}
