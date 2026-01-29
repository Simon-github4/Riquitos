package com.riquitos.pricelist;

import com.riquitos.base.ui.AbstractForm;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class PriceListForm extends AbstractForm<PriceList>{

	TextField name = new TextField("Nombre");
	BigDecimalField marginPercentage = new BigDecimalField("Margen");
	
	public PriceListForm() {
		super(PriceList.class);
		
		marginPercentage.setSuffixComponent(new Span("%")); // Pone el % al final
        marginPercentage.setPlaceholder("0,00");
        
        // Bind automático (busca los campos 'nombre' y 'marginPercentage' en la clase )
        binder.bindInstanceFields(this);

        // Agregamos los componentes visuales
        add(
            name, 
            marginPercentage, 
            createButtonsLayout() // Este método viene del padre
        );
	}

}
