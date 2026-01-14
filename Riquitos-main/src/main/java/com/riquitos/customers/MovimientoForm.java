package com.riquitos.customers;

import java.time.LocalDate;
import java.util.List;

import com.riquitos.base.ui.AbstractForm;
import com.riquitos.customers.Movimiento.MovementType;
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

        // 1. Configurar Combo de Clientes
        cliente.setItems(clientes);
        cliente.setItemLabelGenerator(Customer::getName);
        
        // 2. Configurar Combo de Tipos (Deuda/Pago)
        tipo.setItems(MovementType.values());
        tipo.setItemLabelGenerator(Enum::name);

        // 3. Configurar Monto
        monto.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        
        // 4. Configurar Fecha por defecto hoy
        fecha.setValue(LocalDate.now());

        add(cliente, fecha, tipo, monto, descripcion, createButtonsLayout());

        // Enlace manual o automático. Como "cliente" en la clase Movimiento es un objeto Cliente,
        // y el ComboBox es <Cliente>, bindInstanceFields lo mapea automáticamente.
        binder.bindInstanceFields(this);
    }
    
    public void setClientes(List<Customer> clientes) {
		cliente.setItems(clientes);
	}
}
