package com.riquitos.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.riquitos.base.ui.AbstractForm;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.stock.StockMovement.StockMovementType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.TextRenderer;

public class StockMovementForm extends AbstractForm<StockMovement> {

    private final ComboBox<RawMaterial> rawMaterial = new ComboBox<>("Materia Prima");
    private final RadioButtonGroup<StockMovement.StockMovementType> type = new RadioButtonGroup<>("Tipo de Movimiento");
    private final BigDecimalField quantity = new BigDecimalField("Cantidad");
    private final DateTimePicker movementDateTime = new DateTimePicker("Fecha y Hora");
    private final TextArea observations = new TextArea("Observaciones/Motivo");
    
    // Div para mostrar el stock actual de forma visual
    //private final Div stockStatusPreview = new Div();

    public StockMovementForm(List<RawMaterial> materials) {
        super(StockMovement.class);
        setWidth("65vh");
        configureFields(materials);
        
        add(
            type,
            rawMaterial,
            //stockStatusPreview,
            quantity,
            movementDateTime,
            observations
        );
        
        showButtonDelete(false);
    }

    private void configureFields(List<RawMaterial> materials) {
        type.setItems(StockMovement.StockMovementType.INGRESO, StockMovement.StockMovementType.AJUSTE);
        type.setRenderer(new TextRenderer<>(t -> t == StockMovement.StockMovementType.INGRESO ? "Ingreso (+)" : "Ajuste (+ / -)"));
        type.addValueChangeListener(e -> {
            binder.validate(); 
            if (StockMovementType.INGRESO.equals(e.getValue())) {
                quantity.setHelperText("Ingrese un valor positivo para sumar al stock");
            } else {
                quantity.setHelperText("Use valores positivos (suma) o negativos (resta) para el ajuste");
            }
        });
        
        rawMaterial.setItems(materials);
        rawMaterial.setItemLabelGenerator(RawMaterial::getName);
        rawMaterial.setPlaceholder("Seleccione material...");
        
        //rawMaterial.addValueChangeListener(e -> updateStockPreview());
        //quantity.addValueChangeListener(e -> updateStockPreview());
        //quantity.getElement().setAttribute("inputmode", "text"); 
	    // Opcionalmente, puedes usar "tel", que en muchos celulares muestra los números grandes y el símbolo "+" o "-" más a mano
	    quantity.getElement().setAttribute("inputmode", "tel");
	
	     //((HasAllowedCharPattern) quantity).setAllowedCharPattern("^[0-9.,-]*$");
     
        binder.forField(rawMaterial)
                .asRequired("Debe seleccionar una materia prima")
                .bind(StockMovement::getRawMaterial, StockMovement::setRawMaterial);

        binder.forField(type)
                .asRequired("Seleccione el tipo")
                .bind(StockMovement::getType, StockMovement::setType);

        binder.forField(quantity)
                .asRequired("La cantidad es obligatoria")
                .withValidator(qty -> {
                    if (qty == null) return false;
                    
                    StockMovementType currentType = type.getValue();
                    
                    if (StockMovementType.INGRESO.equals(currentType)) 
                        return qty.compareTo(BigDecimal.ZERO) > 0;
                    
                    return true;}, "Debe ser mayor a cero")
                .bind(StockMovement::getQuantity, StockMovement::setQuantity);

        binder.forField(movementDateTime)
                .asRequired("La fecha es obligatoria")
                .bind(StockMovement::getMovementDateTime, StockMovement::setMovementDateTime);

        binder.forField(observations)
                .bind(StockMovement::getObservations, StockMovement::setObservations);
        
        /* Estilo para la preview de stock
        stockStatusPreview.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-top", "-10px");*/
    }

    /*private void updateStockPreview() {
        stockStatusPreview.removeAll();
        RawMaterial mat = rawMaterial.getValue();
        if (mat != null) {
            Span label = new Span("Stock Actual: ");
            Span val = new Span(mat.getCurrentStock() + " " + mat.getUnit());
            val.getStyle().set("font-weight", "bold");
            stockStatusPreview.add(label, val);
            stockStatusPreview.setVisible(true);
        } else {
            stockStatusPreview.setVisible(false);
        }
    }*/

    @Override
    public void setBean(StockMovement bean) {
        if (bean != null && bean.getMovementDateTime() == null) {
            bean.setMovementDateTime(LocalDateTime.now());
        }
        super.setBean(bean);
    }
    
    public boolean isEliminable(StockMovement movement) {
        return movement != null && movement.getType() != null 
            && movement.getType() != StockMovementType.EGRESO;
    }
}
