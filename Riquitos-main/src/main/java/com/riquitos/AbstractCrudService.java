package com.riquitos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

// T = Entidad (ej: PriceList), ID = Tipo de la PK (ej: Long)
public abstract class AbstractCrudService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    public AbstractCrudService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        return repository.findAll();
        //TODO: Cambiar a paginado cuando se necesite
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Transactional
    public T save(T entity) {
        return repository.saveAndFlush(entity);
    }

    @Transactional
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Transactional
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }

    // Método abstracto para obligar a las clases hijas a implementar la búsqueda
    // ya que cada entidad filtra por campos distintos (nombre, código, etc.)
    public abstract List<T> findAll(String filterText);
}
