package com.riquitos.production;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.product.Product;

import jakarta.annotation.security.PermitAll;

@Route(value = "produccion/batch", layout = MainLayout.class)
@PageTitle("Lotes de Producción | Riquitos")
@Menu(order = 3, icon = "vaadin:factory", title = "Lotes de Producción")
@PermitAll
public class ProductionBatchView extends VerticalLayout {

    private final ProductionBatchService service;
    private final Grid<ProductionBatch> grid;
    private final TextField filterText;

    public ProductionBatchView(ProductionBatchService service) {
        this.service = service;
        this.grid = new Grid<>(ProductionBatch.class, false);
        this.filterText = new TextField();

        configureGrid();
        configureFilter();
        
        add(getToolbar(), grid);
        
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        updateList();
    }

    private void configureGrid() {
        grid.addColumn(batch -> batch.getProduct() != null ? 
            batch.getProduct().getDescription() : "N/A")
            .setHeader("Producto")
            .setAutoWidth(true).setSortable(true)
            .setFlexGrow(1);

        grid.addColumn(batch -> {
            if (batch.getQuantityProduced() != null) {
                return String.format("%.2f unidades", batch.getQuantityProduced());
            }
            return "N/A";
        }).setHeader("Cantidad Producida")
          .setAutoWidth(true);

        grid.addColumn(batch -> {
            if (batch.getProductionDate() != null) {
                return batch.getProductionDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "N/A";
        }).setHeader("Fecha de Producción")
          .setAutoWidth(true).setSortable(true);

        grid.addComponentColumn(batch -> {
            Span status = new Span();
            status.getStyle().set("font-weight", "bold");
            
            if (batch.getQuantityProduced() != null && batch.getQuantityProduced().doubleValue() > 0) {
                status.setText("Completado");
                status.getElement().getThemeList().add("badge success");
            } else {
                status.setText("Pendiente");
                status.getElement().getThemeList().add("badge contrast");
            }
            
            return status;
        }).setHeader("Estado")
          .setAutoWidth(true);

        grid.setSizeFull();
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES);
        
        // Tooltip con información adicional
        grid.setItemDetailsRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(batch -> {
            VerticalLayout details = new VerticalLayout();
            details.setSpacing(false);
            details.setPadding(false);
            
			if (batch.getProduct() != null) {
				com.vaadin.flow.component.html.H3 title = new com.vaadin.flow.component.html.H3(
						"Detalles del Lote #" + batch.getId());
				details.add(title);

				if (batch.getQuantityProduced() != null) {
					BigDecimal costoTotal = batch.getProduct().getCostPrice().multiply(batch.getQuantityProduced());
					NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

					Paragraph pg = new Paragraph("Costo Total del Lote: ");
					Paragraph totalCost = new Paragraph(formatoMoneda.format(costoTotal));
					totalCost.getStyle().set("font-weight", "bold");
					totalCost.getStyle().set("color", "green"); // Opcional: darle color al dinero

					details.add(new HorizontalLayout(pg, totalCost));
				}
			}
            
            return details;
        }));
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filtrar por producto...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        filterText.setWidth("300px");
    }

    private Component getToolbar() {
        return new ViewToolbar("Historial de Lotes de Producción", filterText);
    }

    private void updateList() {
        grid.setItems(service.findAll(filterText.getValue()));
    }
}