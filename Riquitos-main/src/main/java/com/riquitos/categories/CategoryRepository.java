package com.riquitos.categories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

		// If you don't need a total row count, Slice is better than Page as it only
	// performs a select query.
	// Page performs both a select and a count query.
	Slice<Category> findAllBy(Pageable pageable);
}
