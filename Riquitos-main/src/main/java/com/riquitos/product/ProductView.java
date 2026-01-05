package com.riquitos.product;

import java.text.NumberFormat;
import java.util.Locale;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.entities.Product;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "productos", layout = MainLayout.class)
@PageTitle("Productos")
@Menu(order = 2, icon = "vaadin:cube", title = "Productos")
@RolesAllowed({"ADMIN"})
public class ProductView extends AbstractListView<Product, ProductForm, ProductService> {

    public ProductView(ProductService service) {
        super(Product.class, "Listado de Productos", service);
    }

    @Override
    protected ProductForm createForm() {
        return new ProductForm();
    }

    @Override
    protected void configureGrid() {
        // 1. Columna SKU: Ancho fijo porque los códigos suelen ser cortos
        grid.addColumn(Product::getSku)
            .setHeader("SKU")
            .setWidth("200px")
            .setFlexGrow(0); // No se estira

        // 2. Columna Descripción: Ocupa el espacio sobrante
        grid.addColumn(Product::getDescription)
            .setHeader("Descripción")
            .setFlexGrow(1); // Se estira para llenar la pantalla

        // 3. Columna Precio: Formato Moneda Argentina
        grid.addColumn(new NumberRenderer<>(
                Product::getCostPrice,
                NumberFormat.getCurrencyInstance(new Locale("es", "AR")) // Formato $ 1.234,50
            ))
            .setHeader("Costo")
            .setWidth("140px");
            //.setFlexGrow(0);
            
        grid.asSingleSelect().addValueChangeListener(e -> editItem(e.getValue(), true));
    }
}