package com.riquitos.production;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.stock.StockMovement;
import com.riquitos.stock.StockMovementService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "produccion/batch", layout = MainLayout.class)
@PageTitle("Lotes de Producción | Riquitos")
@Menu(order = 3, icon = "vaadin:factory", title = "Lotes de Producción")
@PermitAll
public class ProductionBatchView extends VerticalLayout {

    private final ProductionBatchService service;
    private final StockMovementService stockMovementService;
    private final Grid<ProductionBatch> grid;
    private final TextField filterText;

    public ProductionBatchView(ProductionBatchService service, StockMovementService stockMovementService) {
        this.service = service;
		this.stockMovementService = stockMovementService;
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
                return String.format("%.2f Bolsones / Cajas", batch.getQuantityProduced());
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

        /*grid.addComponentColumn(batch -> {
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
         */
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
					/*BigDecimal costoTotal = batch.getProduct().getCostPrice().multiply(batch.getQuantityProduced());
					NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));*/
					List<StockMovement> movements = stockMovementService.findByProductionBatchId(batch.getId());
					for(StockMovement mov : movements) {
						Paragraph p = new Paragraph(
							(mov.getRawMaterial() != null ? mov.getRawMaterial().getName() : "N/A") +
							" | Cantidad Usada: " + 
							(mov.getQuantity() != null ? mov.getQuantity().toString() : "N/A") +
							(mov.getRawMaterial().getUnit() != null ? " " + mov.getRawMaterial().getUnit() : ""));
							p.getStyle().set("font-weight", "bold");
						details.add(p);
					}
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