package com.riquitos.categories;

import com.riquitos.base.ui.AbstractForm;
import com.vaadin.flow.component.textfield.TextField;

public class CategoryForm extends AbstractForm<Category>{

	TextField name = new TextField("Nombre");

	public CategoryForm() {
		super(Category.class);

		binder.bindInstanceFields(this);

        // Agregamos los componentes visuales
        add(
            name
        );
	}

}
