package com.riquitos.product;

import java.util.Locale;

import com.riquitos.base.ui.AbstractForm;
import com.riquitos.entities.Product;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class ProductForm extends AbstractForm<Product> {

    // Los nombres de variables deben coincidir EXACTAMENTE con la entidad
    // para que bindInstanceFields funcione automático.
    TextField sku = new TextField("SKU / Código");
    TextField description = new TextField("Descripción");
    BigDecimalField costPrice = new BigDecimalField("Precio Costo");

    public ProductForm() {
        super(Product.class);

        // --- Configuración Visual ---
        
        // 1. SKU: Ancho completo
        sku.setWidthFull();

        // 2. Precio: Formato Moneda
        costPrice.setPrefixComponent(new Span("$")); // Agrega el símbolo $ visualmente
        //costPrice.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT); // Números a la derecha
        costPrice.setLocale(new Locale("es", "AR")); // Fuerza la coma decimal para input
        costPrice.setPlaceholder("0,00");

        // --- Binding ---
        // Conecta las variables visuales con los atributos de Product
        binder.bindInstanceFields(this);

        // --- Layout ---
        add(
            sku, 
            description, 
            costPrice,
            createButtonsLayout() // Botones Guardar/Cancelar del padre
        );
    }
}