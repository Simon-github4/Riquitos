package com.riquitos.product;

import java.text.NumberFormat;
import java.util.Locale;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.categories.CategoryService;
import com.riquitos.production.material.RawMaterialService;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "productos", layout = MainLayout.class)
@PageTitle("Productos")
@Menu(order = 2, icon = "vaadin:package", title = "Productos")
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class ProductView extends AbstractListView<Product, ProductForm, ProductService> {
	
	private final RawMaterialService rawMaterialService;
	private final CategoryService categoryService;
	
    public ProductView(ProductService service, RawMaterialService rawMaterialService, CategoryService categoryService) {
    	super(Product.class, "Listado de Productos", service);
    	this.rawMaterialService = rawMaterialService;
    	this.categoryService = categoryService;
    }

    @Override
    protected ProductForm createForm() {
        return new ProductForm(rawMaterialService, categoryService);
    }

    @Override
    protected void configureGrid() {
        grid.addColumn(Product::getSku)
            .setHeader("SKU")
            .setAutoWidth(true)
            .setFlexGrow(0); 

        grid.addColumn(Product::getDescription)
            .setHeader("Descripción")
            .setWidth("200px")
            .setFlexGrow(1); 

        grid.addColumn(Product::getNetWeight)
        .setHeader("Peso (g) x unidad")
        .setAutoWidth(true)
        .setFlexGrow(0); 
        
        grid.addColumn(Product::getUnitiesPerBagOrBox)
        .setHeader("Unidades x Bolson / Caja")
        .setAutoWidth(true)
        .setFlexGrow(0); 
      
        
        grid.addColumn(new NumberRenderer<>(
                Product::getCostPrice,
                NumberFormat.getCurrencyInstance(new Locale("es", "AR")) // Formato $ 1.234,50
            ))
            .setHeader("Costo")
            .setAutoWidth(true)
            .setFlexGrow(0);
            
        grid.asSingleSelect().addValueChangeListener(e -> editItem(e.getValue(), true));
    }
}