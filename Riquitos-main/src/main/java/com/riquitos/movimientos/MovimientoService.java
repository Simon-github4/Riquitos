package com.riquitos.movimientos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riquitos.AbstractCrudService;
import com.riquitos.customers.Customer;

@Service
public class MovimientoService extends AbstractCrudService<Movimiento, Long> {

    private final MovimientoRepository movimientoRepository;

    public MovimientoService(MovimientoRepository movimientoRepository) {
        super(movimientoRepository);
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> findAll(String filterText) {
        //if (filterText == null || filterText.trim().isEmpty()) 
            return movimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
        
        /* Usamos Specification para buscar coincidencias en la descripción
        return movimientoRepository.findAll((root, query, cb) -> 
            cb.like(cb.lower(root.get("descripcion")), "%" + filterText.toLowerCase() + "%"),
            Sort.by(Sort.Direction.DESC, "fecha")
        );*/
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findAllByCliente(Long clienteId) {
        return movimientoRepository.findByClienteIdOrderByFechaDescIdDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findAllByClienteAndFechas(Long clienteId, LocalDate desde, LocalDate hasta) {
        return movimientoRepository.findAll((root, query, cb) -> {
            var predicate = cb.conjunction(); // AND inicial

            // 1. Filtrar por Cliente
            predicate.getExpressions().add(cb.equal(root.get("cliente").get("id"), clienteId));

            // 2. Filtrar por Fecha Desde (si existe)
            if (desde != null) {
                predicate.getExpressions().add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }

            // 3. Filtrar por Fecha Hasta (si existe)
            if (hasta != null) {
                predicate.getExpressions().add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }

            // 4. Ordenamiento (Importante para que se vea bien)
            query.orderBy(cb.desc(root.get("fecha")), cb.desc(root.get("id")));

            return predicate;
        });
    }
    
    @Override
    @Transactional
    public void delete(Movimiento movFormulario) {
        // 1. Buscamos la entidad "viva" en la base de datos(El objeto que viene del formulario suele estar "desconectado")
        Movimiento movReal = repository.findById(movFormulario.getId()).orElse(null);

        if (movReal != null) {
            // 2. DESVINCULAR DEL PADRE (El paso clave)
            Customer padre = movReal.getCliente();
            
            if (padre != null) // Usamos removeIf por ID para evitar problemas con el equals/hashCode de Lombok
                padre.getMovimientos().removeIf(m -> m.getId().equals(movReal.getId()));

            repository.delete(movReal);
        }
    }
}
