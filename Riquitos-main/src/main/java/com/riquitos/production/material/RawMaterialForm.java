package com.riquitos.production.material;

import com.riquitos.base.ui.AbstractForm;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class RawMaterialForm extends AbstractForm<RawMaterial> {

    TextField name = new TextField("Nombre");
    TextField unit = new TextField("Unidad (Kg, L, Uni)");
    BigDecimalField currentStock = new BigDecimalField("Stock Actual");

    public RawMaterialForm() {
        super(RawMaterial.class);
        
        // Configuraciones visuales extra
        currentStock.setHelperText("Cantidad disponible en depósito");
        
        // Añadimos los campos y los botones (que vienen del padre)
        add(
            name,
            unit, 
            currentStock
        );
        currentStock.setReadOnly(true);
        
        binder.bindInstanceFields(this);

    }
}