package com.riquitos.pricelist;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.product.Product;
import com.riquitos.product.ProductForm;
import com.riquitos.product.ProductService;
import com.riquitos.production.material.RawMaterialService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "listas", layout = MainLayout.class)
@PageTitle("Listas de precios")
@Menu(order = 1, icon = "vaadin:invoice", title = "Listas de precios")
@RolesAllowed({"VENDEDOR", "ADMIN"})
public class PriceListProductsView
    extends AbstractListView<Product, ProductForm, ProductService> {

    private final PriceListService priceListService;
	private final RawMaterialService rawMaterialService;

    private PriceList selectedPriceList;
    private ComboBox<PriceList> priceListCombo = new ComboBox<>();
	private Button priceListButton;

    public PriceListProductsView(
            ProductService productService,
            PriceListService priceListService, RawMaterialService rawMaterialService
    ) {
        super(Product.class, "Listas de precios", productService);
        this.priceListService = priceListService;
		this.rawMaterialService = rawMaterialService;
        priceListCombo.setItems(priceListService.findAll());

    }

    @Override
    protected ProductForm createForm() {
        return null;//new ProductForm(rawMaterialService);
    }

    @Override
    protected void configureGrid() {
        grid.addColumn(Product::getDescription)
            .setHeader("Producto")
            .setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(
                Product::getCostPrice,
                "$ %.2f",
                Locale.forLanguageTag("es-AR")
        ))
        .setHeader("Costo")
        .setAutoWidth(true);

        grid.addColumn(new NumberRenderer<>(
                product -> calcularPrecioVenta(product), 
                "$ %.2f",
                Locale.forLanguageTag("es-AR")
        ))
        .setHeader("Precio venta")
        .setAutoWidth(true);

        /*grid.asSingleSelect().addValueChangeListener(e ->
            editItem(e.getValue(), true)
        );*/
    }

    // TOOLBAR PERSONALIZADO (reemplaza filtro texto)
    @Override
    protected Component getToolbar(String title) {

        //priceListCombo = new ComboBox<>();
        priceListCombo.setPlaceholder("Seleccionar lista de precios");
        priceListCombo.setItemLabelGenerator(PriceList::getName);
        priceListCombo.setClearButtonVisible(true);
        priceListCombo.setMaxWidth("250px");

        priceListCombo.addValueChangeListener(e -> {
            selectedPriceList = e.getValue();
            updateList();
            updatePriceListButton();
        });

        priceListButton = new Button("Lista", new Icon(VaadinIcon.PLUS));
        priceListButton.addClickListener(e -> openPriceListDialog());
        updatePriceListButton(); // estado inicial

        return new ViewToolbar(
            title,
            ViewToolbar.group(priceListCombo, priceListButton)
        );
    }

    @Override
    protected void updateList() {
        if (selectedPriceList == null) {
            grid.setItems(Collections.emptyList());
            return;
        }

        grid.setItems(service.findAll());
    }
    
    private void openPriceListDialog() {
        Dialog dialog = new Dialog();
        PriceListForm form = new PriceListForm();

        boolean editing = selectedPriceList != null;
        
        dialog.setHeaderTitle(editing ? "Editar lista de precios" : "Nueva lista de precios");
        form.setBean(editing ? selectedPriceList : new PriceList());

       	form.showButtonDelete(editing);
        
        form.addSaveListener(e -> {
            PriceList saved = priceListService.save(e.getBean());

            dialog.close();

            refreshPriceListCombo(saved);

            Notification.show(
                editing ? "Lista actualizada" : "Lista creada correctamente",
                3000,
                Notification.Position.TOP_CENTER
            );
        });

        form.addDeleteListener(e -> {
            try {
                priceListService.delete(e.getBean());
                dialog.close();

                refreshPriceListCombo(null);
                selectedPriceList = null;
                updateList();

                Notification.show(
                    "Lista eliminada",
                    3000,
                    Notification.Position.TOP_CENTER
                );
            } catch (Exception ex) {
                Notification.show(
                    "No se puede eliminar: la lista está en uso",
                    5000,
                    Notification.Position.TOP_CENTER
                );
            }
        });

        form.addCloseListener(e -> dialog.close());

        dialog.add(form);
        dialog.open();
    }
    private void refreshPriceListCombo(PriceList select) {
        priceListCombo.setItems(priceListService.findAll(""));
        priceListCombo.setValue(select);
    }

    private void updatePriceListButton() {
        if (selectedPriceList == null) {
            priceListButton.setText("Nueva lista");
            priceListButton.setIcon(new Icon(VaadinIcon.PLUS));
        } else {
            priceListButton.setText("Editar lista");
            priceListButton.setIcon(new Icon(VaadinIcon.EDIT));
        }
    }

    private BigDecimal calcularPrecioVenta(Product product) {
        if (selectedPriceList == null || product.getCostPrice() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal factor = BigDecimal.ONE.add(
            selectedPriceList.getMarginPercentage()
                .divide(BigDecimal.valueOf(100))
        );

        return product.getCostPrice().multiply(factor);
    }
    
}
