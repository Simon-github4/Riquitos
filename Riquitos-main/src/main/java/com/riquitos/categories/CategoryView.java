package com.riquitos.categories;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.customers.Customer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "categories", layout = MainLayout.class)
@PageTitle("Categorias")
@Menu(order = 8, icon = "vaadin:tags", title = "Categorias")
@RolesAllowed({"VENDEDOR", "ADMIN"})
public class CategoryView extends AbstractListView<Category, CategoryForm, CategoryService>{

	public CategoryView(CategoryService service) {
		super(Category.class, "Categorias", service);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected CategoryForm createForm() {
		return new CategoryForm();
	}

	@Override
	protected void configureGrid() {
		grid.setSizeFull();
		grid.addColumn(Category::getId).setHeader("Codigo");
		grid.addColumn(Category::getName).setHeader("Nombre").setSortable(true);
		
        grid.asSingleSelect().addValueChangeListener(event -> editItem(event.getValue(), true));

	}

	

}
