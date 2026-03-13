package com.riquitos.product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	Slice<Product> findAllBy(Pageable pageable);
	
	List<Product> findByDescriptionContainingIgnoreCase(String filterText);
	
	@Query("SELECT p FROM Product p ORDER BY CASE WHEN p.imageData IS NULL THEN 1 ELSE 0 END ASC, p.description ASC")
	List<Product> findAllOrderByImagePresentAndDescription();
	
}