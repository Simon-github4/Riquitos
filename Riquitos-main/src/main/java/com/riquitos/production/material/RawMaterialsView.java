package com.riquitos.production.material;

import java.math.BigDecimal;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.BadgeUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PageTitle("Insumos")
@Route(value = "insumos", layout = com.riquitos.base.ui.MainLayout.class)
@Menu(order = 6, icon = "vaadin:cubes", title = "Insumos")
@PermitAll
public class RawMaterialsView extends AbstractListView<RawMaterial, RawMaterialForm, RawMaterialService> {

    public RawMaterialsView(RawMaterialService service) {
        super(RawMaterial.class, "Gestión de Insumos", service);
    }

    @Override
    protected RawMaterialForm createForm() {
        return new RawMaterialForm();
    }

    @Override
    protected void configureGrid() {
        grid.addClassName("raw-material-grid");
        grid.addColumn(RawMaterial::getName).setHeader("Nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(RawMaterial::getUnit).setHeader("Unidad").setSortable(true).setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(material -> {
            BigDecimal stock = material.getCurrentStock();
            Span span = new Span(stock != null ? stock.toString() : "0");
            
            if (stock != null && stock.compareTo(BigDecimal.ZERO) < 0)
            	span.getElement().getThemeList().add("badge error");
            	else if (stock != null && stock.compareTo(BigDecimal.TEN) < 0) 
            		BadgeUtils.setWarningTheme(span, false); 
            		else 
            			span.getElement().getThemeList().add("badge success"); 
            
            return span;
        })).setHeader("Stock Actual").setSortable(false).setAutoWidth(true);

        /*grid.addComponentColumn(material -> {
            Button addStockBtn = new Button(VaadinIcon.PLUS.create());
            addStockBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            addStockBtn.setTooltipText("Registrar compra / Ingreso de stock");
            addStockBtn.addClickListener(e -> abrirDialogoIngreso(material));
            return addStockBtn;
        }).setHeader("Ingresar").setWidth("100px");
         AHORA DESDE STOCK MOVEMENTS*/
        grid.asSingleSelect().addValueChangeListener(event ->
            editItem(event.getValue(), true)
        );
    }

    // --- LÓGICA DE INGRESO DE STOCK ---
    private void abrirDialogoIngreso(RawMaterial material) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ingreso: " + material.getName());

        BigDecimalField cantidadInput = new BigDecimalField("Cantidad a sumar");
        cantidadInput.setSuffixComponent(new Span(material.getUnit()));
        cantidadInput.setPlaceholder("0.00");
        cantidadInput.focus();
        cantidadInput.setWidthFull();

        Button confirmarBtn = new Button("Confirmar Ingreso", e -> {
            BigDecimal cantidad = cantidadInput.getValue();
            if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0) {
                guardarIngreso(material, cantidad);
                dialog.close();
            } else {
                Notification.show("Ingrese una cantidad válida")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmarBtn.setWidthFull();
        confirmarBtn.addClickShortcut(Key.ENTER);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());
        cancelarBtn.setWidthFull();

        VerticalLayout layout = new VerticalLayout(cantidadInput, confirmarBtn, cancelarBtn);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(layout);
        dialog.open();
    }

    private void guardarIngreso(RawMaterial material, BigDecimal cantidadIngresada) {
        try {
            // 1. Calcular nuevo stock
            BigDecimal stockActual = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal nuevoStock = stockActual.add(cantidadIngresada);
            
            // 2. Actualizar objeto
            material.setCurrentStock(nuevoStock);
            
            // 3. Guardar en BD (Usando el servicio que ya tienes en la clase padre)
            service.save(material);
            
            // 4. Actualizar UI
            updateList();
            
            Notification.show("Se agregaron " + cantidadIngresada + " " + material.getUnit(), 
                    3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error actualizando stock", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
