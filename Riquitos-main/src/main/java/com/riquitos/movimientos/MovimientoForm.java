package com.riquitos.movimientos;

import java.time.LocalDate;
import java.util.List;

import com.riquitos.base.ui.AbstractForm;
import com.riquitos.customers.Customer;
import com.riquitos.movimientos.Movimiento.MovementType;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class MovimientoForm extends AbstractForm<Movimiento> {

    // Componentes
    private ComboBox<Customer> cliente = new ComboBox<>("Cliente");
    private DatePicker fecha = new DatePicker("Fecha Operación");
    private ComboBox<MovementType> tipo = new ComboBox<>("Tipo");
    private BigDecimalField monto = new BigDecimalField("Monto");
    private TextField descripcion = new TextField("Descripción");

    public MovimientoForm(List<Customer> clientes) {
        super(Movimiento.class);

        cliente.setItems(clientes);
        cliente.setItemLabelGenerator(Customer::getName);
        
        tipo.setItems(MovementType.values());
        tipo.setItemLabelGenerator(Enum::name);

        monto.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        
        add(cliente, fecha, tipo, monto, descripcion);

        // Enlace manual o automático. Como "cliente" en la clase Movimiento es un objeto Cliente,
        // y el ComboBox es <Cliente>, bindInstanceFields lo mapea automáticamente.
        binder.bindInstanceFields(this);

    }
    
    public void setClientes(List<Customer> clientes) {
		cliente.setItems(clientes);
	}
}
