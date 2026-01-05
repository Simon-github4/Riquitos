package com.riquitos.pricelist;

import java.util.List;

import org.springframework.stereotype.Service;

import com.riquitos.AbstractCrudService;
import com.riquitos.entities.PriceList;

@Service
public class PriceListService extends AbstractCrudService<PriceList, Long> {

    // Necesitamos el repo concreto para acceder a sus métodos específicos
    private final PriceListRepository priceListRepository;

    public PriceListService(PriceListRepository repository) {
        super(repository); // Pasamos el repo al padre
        this.priceListRepository = repository;
    }

    @Override
    public List<PriceList> findAll(String filterText) {
        //if (filterText == null || filterText.isEmpty()) {
            return repository.findAll();
        /*} else {
            return priceListRepository.searchByNameContainingIgnoreCase(filterText);
        }*/
    }
}