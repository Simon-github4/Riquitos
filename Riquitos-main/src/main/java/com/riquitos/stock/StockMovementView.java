package com.riquitos.stock;

import java.time.format.DateTimeFormatter;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;
import com.riquitos.stock.StockMovement.StockMovementType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

import jakarta.annotation.security.RolesAllowed;

@Route(value = "stock/movimientos", layout = MainLayout.class)
@PageTitle("Kardex de Stock | Riquitos")
@Menu(order = 4, icon = "vaadin:exchange", title = "Kardex de Stock")
@RolesAllowed({"ADMIN"})
public class StockMovementView extends VerticalLayout {

    private final StockMovementService service;
    private final RawMaterialService rawMaterialService;
    private final Grid<StockMovement> grid;
    
    private final TextField filterText;
    private final ComboBox<RawMaterial> materialFilter;
    private final ComboBox<StockMovementType> typeFilter;
    
    private final StockMovementForm movementForm;
    private final Dialog dialog = new Dialog();

    public StockMovementView(StockMovementService service, RawMaterialService rawMaterialService) {
        this.service = service;
        this.rawMaterialService = rawMaterialService;
        this.grid = new Grid<>(StockMovement.class, false);
        this.filterText = new TextField();
        this.materialFilter = new ComboBox<>();
        this.typeFilter = new ComboBox<>();
        
		this.movementForm = new StockMovementForm(rawMaterialService.findAll());
		
		configureDialog();
        configureGrid();
        configureFilters();
        
        add(getToolbar(), getFilters(), grid);
        
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        updateList();
    }

    private void configureGrid() {
	    grid.addColumn(movement -> movement.getMovementDateTime() != null ? 
	            movement.getMovementDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A")
	        .setHeader("Fecha")
	        .setWidth("160px") 
	        .setFlexGrow(0)
	        .setSortable(true);
	
	    grid.addComponentColumn(movement -> {
	        HorizontalLayout badgeLayout = new HorizontalLayout();
	        badgeLayout.setSpacing(true); // Un poco de aire entre icono y texto
	        badgeLayout.setAlignItems(Alignment.CENTER);
	        
	        StockMovementType type = movement.getType();
	        if (type != null) {
	            Icon icon;
	            Span badge = new Span();
	            switch (type) {
	                case INGRESO:
	                    icon = VaadinIcon.ARROW_DOWN.create();
	                    icon.getStyle().set("color", "var(--lumo-success-color)");
	                    badge.setText("INGRESO");
	                    badge.getElement().getThemeList().add("badge success");
	                    badgeLayout.add(icon, badge);
	                    break;
	                case EGRESO:
	                    icon = VaadinIcon.ARROW_UP.create();
	                    icon.getStyle().set("color", "var(--lumo-error-color)");
	                    badge.setText("EGRESO");
	                    badge.getElement().getThemeList().add("badge error");
	                    badgeLayout.add(icon, badge);
	                    break;
	                case AJUSTE:
	                    icon = VaadinIcon.EDIT.create();
	                    icon.getStyle().set("color", "var(--lumo-contrast-60pct)");
	                    badge.setText("AJUSTE");
	                    badge.getElement().getThemeList().add("badge contrast");
	                    badgeLayout.add(icon, badge);
	                    break;
	            }
	        }
	        return badgeLayout;
	    }).setHeader("Tipo")
	      .setWidth("140px")
	      .setFlexGrow(0);
	
	    grid.addColumn(movement -> movement.getRawMaterial() != null ? movement.getRawMaterial().getName() : "N/A")
	        .setHeader("Materia Prima")
	        .setWidth("200px")
	        .setSortable(true)
	        .setAutoWidth(true)
	        .setFlexGrow(0);
	
	    grid.addComponentColumn(movement -> {
	        Div quantityDiv = new Div();
	        if (movement.getQuantity() != null) {
	            String sign = "";
	            String color = "inherit";
	            if (movement.getType() == StockMovementType.INGRESO) { sign = "+"; color = "var(--lumo-success-text-color)"; }
	            else if (movement.getType() == StockMovementType.EGRESO) { sign = "-"; color = "var(--lumo-error-text-color)"; }
	            
	            String unit = (movement.getRawMaterial() != null) ? " " + movement.getRawMaterial().getUnit() : "";
	            quantityDiv.setText(sign + movement.getQuantity() + unit);
	            quantityDiv.getStyle().set("color", color).set("font-weight", "600").set("text-align", "right");
	        }
	        return quantityDiv;
	    }).setHeader("Cantidad")
	      .setTextAlign(ColumnTextAlign.END) // Alineación contable
	      .setWidth("120px")
	      .setFlexGrow(0);
	
	    grid.addColumn(movement -> {
	        if (movement.getProductionBatch() != null) {
	            return "Lote #" + movement.getProductionBatch().getId();
	        }
	        return "-";
	    }).setHeader("Ref.")
	      .setWidth("125px")
	      .setFlexGrow(0);
	
 	    grid.addColumn(StockMovement::getObservations)
 	        .setHeader("Observaciones")
 	        .setAutoWidth(true)
 	        .setFlexGrow(2);
 	    
 	   grid.addComponentColumn(movement -> {
 		    Div container = new Div();
 		    container.getStyle()
 		        .set("display", "flex")
 		        .set("align-items", "center")
 		        .set("justify-content", "center")
 		        .set("height", "32px"); // Altura fija pequeña para que no expanda la fila
 		    
 		    if (movement.getType() == StockMovementType.EGRESO) {
 		        Icon infoIcon = VaadinIcon.INFO.create();
 		        infoIcon.setColor("var(--lumo-primary-color)");
 		        infoIcon.setSize("18px"); // Tamaño controlado
 		        infoIcon.getTooltip().setText("Si necesitas eliminar un egreso, debes eliminar el Lote de producción");
 		        
 		        container.add(infoIcon);
 		        container.getStyle().set("cursor", "help");
 		    } else {
 		        Icon deleteIcon = VaadinIcon.TRASH.create();
 		        deleteIcon.setColor("var(--lumo-error-text-color)");
 		        deleteIcon.setSize("18px");
 		        deleteIcon.getStyle() .set("cursor", "pointer");
 		        deleteIcon.getTooltip().setText("Eliminar movimiento");
 		        deleteIcon.addClickListener(e -> confirmarEliminacion(movement));
 		        
 		        container.add(deleteIcon);
 		    }
 		    
 		    return container;
 		}).setHeader("Acciones")
 		  .setWidth("90px")
 		  .setFlexGrow(0);
 	
 	    grid.setSizeFull();
	    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
    }

    private void configureFilters() {
        filterText.setPlaceholder("Buscar en observaciones...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        filterText.setWidth("300px");

        materialFilter.setItems(rawMaterialService.findAll(""));
        materialFilter.setItemLabelGenerator(RawMaterial::getName);
        materialFilter.setPlaceholder("Todas las materias primas");
        materialFilter.setClearButtonVisible(true);
        materialFilter.setWidth("250px");
        materialFilter.addValueChangeListener(e -> updateList());

        typeFilter.setItems(StockMovementType.values());
        typeFilter.setPlaceholder("Todos los tipos");
        typeFilter.setClearButtonVisible(true);
        typeFilter.setWidth("200px");
        typeFilter.addValueChangeListener(e -> updateList());

        Button clearFilters = new Button("Limpiar filtros", new Icon(VaadinIcon.CLOSE_CIRCLE));
        clearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFilters.addClickListener(e -> clearFilters());
    }


    private Component getToolbar() {
        Button ingresoButton = new Button("Registrar Ingreso / Ajuste", new Icon(VaadinIcon.PLUS_CIRCLE));
        ingresoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        ingresoButton.addClickListener(e -> openForm(null));
        
        HorizontalLayout buttons = new HorizontalLayout(ingresoButton);
        return new ViewToolbar("Kardex de Stock", buttons);
    }

    private Component getFilters() {
        Button clearFiltersBtn = new Button("Limpiar", new Icon(VaadinIcon.CLOSE_CIRCLE));
        clearFiltersBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersBtn.addClickListener(e -> clearFilters());

        HorizontalLayout filters = new HorizontalLayout(
            filterText,
            materialFilter,
            typeFilter,
            clearFiltersBtn
        );
        filters.setWidthFull();
        filters.setAlignItems(Alignment.END);
        filters.setPadding(true);
        filters.setSpacing(true);
        filters.setWrap(true);
        
        return filters;
    }

    private void updateList() {
        var movements = service.findAll("");
        
        if (materialFilter.getValue() != null) {
            movements = movements.stream()
                .filter(m -> m.getRawMaterial() != null && 
                           m.getRawMaterial().getId().equals(materialFilter.getValue().getId()))
                .toList();
        }
        
        if (typeFilter.getValue() != null) {
            movements = movements.stream()
                .filter(m -> m.getType() == typeFilter.getValue())
                .toList();
        }
        
        if (filterText.getValue() != null && !filterText.getValue().isEmpty()) {
            String filter = filterText.getValue().toLowerCase();
            movements = movements.stream()
                .filter(m -> m.getObservations() != null && 
                           m.getObservations().toLowerCase().contains(filter))
                .toList();
        }
        
        grid.setItems(movements);
    }

    private void clearFilters() {
        filterText.clear();
        materialFilter.clear();
        typeFilter.clear();
        updateList();
    }
    
    private void configureDialog() {
        dialog.setHeaderTitle("Movimiento de Stock");
        dialog.add(movementForm);
        
        movementForm.addSaveListener(event -> {
            saveMovement(event.getBean());
        });

        movementForm.addCloseListener(e -> dialog.close());
    }

    private void saveMovement(StockMovement movement) {
        try {
            if (movement.getType() == StockMovementType.INGRESO) {
                service.saveIngreso(
                    movement.getRawMaterial(),
                    movement.getQuantity(),
                    movement.getObservations()
                );
            } else if (movement.getType() == StockMovementType.AJUSTE) {
                service.saveAjuste(
                    movement.getRawMaterial(),
                    movement.getQuantity(),
                    movement.getObservations()
                );
            } else {
                service.saveEgreso(
                		movement.getRawMaterial(),
                		movement.getQuantity(),
                		movement.getProductionBatch(),
                        movement.getObservations());
            }
            
            showSuccess("Movimiento registrado correctamente");
            updateList();
            dialog.close();
            
        } catch (Exception e) {
            showError("Error al guardar: " + e.getMessage());
        }
    }

    private void openForm(StockMovementType defaultType) {
        StockMovement movement = new StockMovement();
        movement.setType(defaultType);
        
        movementForm.setBean(movement);
        dialog.open();
    }
    
    private void deleteMovement(StockMovement movement) {
        try {
            service.delete(movement);
            showSuccess("Movimiento eliminado correctamente");
            updateList();
            dialog.close();
        } catch (Exception e) {
            showError("Error al eliminar: " + e.getMessage());
        }
    }
    
    private void confirmarEliminacion(StockMovement movement) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Eliminar Movimiento");
        confirmDialog.setText("¿Está seguro que desea eliminar este movimiento de stock?\n\n" +
            "Tipo: " + movement.getType() + "\n" +
            "Cantidad: " + movement.getQuantity() + "\n" +
            "Materia Prima: " + (movement.getRawMaterial() != null ? movement.getRawMaterial().getName() : "N/A") + "\n\n" +
            "El stock será recalculado automáticamente.");
        
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancelar");
        confirmDialog.setConfirmText("Eliminar");
        confirmDialog.setConfirmButtonTheme("error primary");
        
        confirmDialog.addConfirmListener(event -> {
            deleteMovement(movement);
        });
        
        confirmDialog.open();
    }

    // --- Métodos de Notificación Auxiliares ---
    private void showSuccess(String msg) {
        Notification n = Notification.show(msg);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}