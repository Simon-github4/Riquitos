package com.riquitos.production;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.stock.StockMovement;
import com.riquitos.stock.StockMovementService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "produccion/batch", layout = MainLayout.class)
@PageTitle("Lotes de Producción | Riquitos")
@Menu(order = 3, icon = "vaadin:factory", title = "Lotes de Producción")
@RolesAllowed({"ADMIN"})
public class ProductionBatchView extends VerticalLayout {

    private final ProductionBatchService service;
    private final StockMovementService stockMovementService;
    private final Grid<ProductionBatch> grid;
    
    // Filtros
    private final TextField filterText;
    private final DatePicker dateFrom;
    private final DatePicker dateTo;

    public ProductionBatchView(ProductionBatchService service, StockMovementService stockMovementService) {
        this.service = service;
        this.stockMovementService = stockMovementService;
        this.grid = new Grid<>(ProductionBatch.class, false);
        
        // Inicializar componentes de filtro
        this.filterText = new TextField();
        this.dateFrom = new DatePicker("Desde");
        this.dateTo = new DatePicker("Hasta");

        configureGrid();
        configureFilter();
        
        // Añadimos el Toolbar (que contiene el título y los filtros agrupados)
        add(getToolbar(), grid);
        
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES);
        
        grid.addColumn(batch -> batch.getProduct() != null ? 
            batch.getProduct().getDescription() : "N/A")
            .setHeader("Producto")
            .setAutoWidth(true).setSortable(true)
            .setFlexGrow(1);

        grid.addColumn(batch -> {
            if (batch.getUnitiesProduced() != null) {
                return String.format("%.2f Unidades", batch.getUnitiesProduced());
            }
            return "N/A";
        }).setHeader("Unidades Producida")
          .setAutoWidth(true);

        grid.addColumn(batch -> {
            if (batch.getBagsOrBoxProduced() != null) {
                return String.format("%.2f Bolsones/Cajas", batch.getBagsOrBoxProduced());
            }
            return "N/A";
        }).setHeader("Bolsones/Cajas Producida")
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
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            
            deleteBtn.addClickListener(e -> confirmarEliminacion(batch));
            
            return deleteBtn;
        }).setHeader("Acciones").setAutoWidth(true);
        
        grid.setItemDetailsRenderer(new com.vaadin.flow.data.renderer.ComponentRenderer<>(batch -> {
            VerticalLayout details = new VerticalLayout();
            details.setSpacing(false);
            details.setPadding(false);
            
            if (batch.getProduct() != null) {
                details.add(new com.vaadin.flow.component.html.H3("Detalles del Lote #" + batch.getId()));
                
                BigDecimal weightGrams = BigDecimal.valueOf(batch.getProduct().getNetWeight());
				BigDecimal batchKg = batch.getUnitiesProduced().multiply(weightGrams).divide(new BigDecimal("1000"));
				
				Paragraph p = new Paragraph("Peso Total Producido: " + batchKg.setScale(2, BigDecimal.ROUND_HALF_UP) + " kg");
				 p.getStyle().set("font-weight", "bold");
				 details.add(p);
				 
                if (batch.getUnitiesProduced() != null) {
                    List<StockMovement> movements = stockMovementService.findByProductionBatchId(batch.getId());
                    for(StockMovement mov : movements) {
                        p = new Paragraph(
                            (mov.getRawMaterial() != null ? mov.getRawMaterial().getName() : "N/A") +
                            " | Cantidad Usada: " + 
                            (mov.getQuantity() != null ? mov.getQuantity().toString() : "N/A") +
                            (mov.getRawMaterial().getUnit() != null ? " " + mov.getRawMaterial().getUnit() : ""));
                        details.add(p);
                    }
                }
            }
            
            return details;
        }));
    }

    private void configureFilter() {
        // Configuración del filtro de texto
        filterText.setPlaceholder("Filtrar por producto...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        filterText.setWidth("300px");

        // Configuración de los DatePickers
        dateFrom.setClearButtonVisible(true);
        dateFrom.addValueChangeListener(e -> {
            // Validar que 'Hasta' no sea menor que 'Desde'
            if(dateTo.getValue() != null && dateFrom.getValue() != null && dateTo.getValue().isBefore(dateFrom.getValue())){
                dateTo.setValue(null);
            }
            updateList();
        });

        dateTo.setClearButtonVisible(true);
        dateTo.addValueChangeListener(e -> {
             // Validar que 'Desde' no sea mayor que 'Hasta'
            if(dateFrom.getValue() != null && dateTo.getValue() != null && dateFrom.getValue().isAfter(dateTo.getValue())){
                dateFrom.setValue(null);
            }
            updateList();
        });
    }

    private Component getToolbar() {
        HorizontalLayout filterLayout = new HorizontalLayout(filterText, dateFrom, dateTo);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE); // Alineación visual
        filterLayout.setWrap(true);
        
        return new ViewToolbar("Historial de Lotes de Producción", filterLayout);
    }

    private void updateList() {
        grid.setItems(service.findAll(filterText.getValue(), dateFrom.getValue(), dateTo.getValue()));
    }
    
    private void confirmarEliminacion(ProductionBatch batch) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Anular Producción");
        dialog.setText(String.format("¿Estás seguro que deseas anular la producción de %s del %s? \n\n" +
                          "Esto devolverá las materias primas al stock.", 
                          batch.getProduct().getDescription(),
                          batch.getProductionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        );
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        
        dialog.setConfirmText("Anular y Restaurar Stock");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> eliminarProduccion(batch));
        dialog.open();
    }

    private void eliminarProduccion(ProductionBatch batch) {
        try {
            service.anularProduccion(batch);
            updateList();
            
            Notification notification = Notification.show("Producción anulada y stock restaurado correctamente.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
        } catch (Exception e) {
            e.printStackTrace();
            Notification notification = Notification.show("Error al anular la producción: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}