package com.riquitos.customers;

import com.riquitos.base.ui.AbstractForm;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;

public class ClienteForm extends AbstractForm<Customer> {

    TextField name = new TextField("Nombre");
    EmailField email = new EmailField("Email");
    TextField phone = new TextField("Teléfono");

    public ClienteForm() {
        super(Customer.class);
        
        // Configuraciones visuales extras
        name.setRequired(true);
        email.setClearButtonVisible(true);
        
        // Agregamos los componentes al FormLayout (clase padre)
        add(name, email, phone);
        
        // Enlace automático: Los nombres de las variables (nombre, email...) 
        // deben coincidir con los atributos de la entidad Cliente
        binder.bindInstanceFields(this);
    }
}