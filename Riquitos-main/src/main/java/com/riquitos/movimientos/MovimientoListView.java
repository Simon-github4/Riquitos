package com.riquitos.movimientos;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar; // Asegúrate de importar tu ViewToolbar
import com.riquitos.customers.Customer;
import com.riquitos.customers.CustomerService;
import com.riquitos.movimientos.Movimiento.MovementType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "movimientos", layout = MainLayout.class)
@PageTitle("Movimientos | Riquitos")
@Menu(order = 5, icon = "vaadin:exchange", title = "Movimientos")
@PermitAll
public class MovimientoListView extends AbstractListView<Movimiento, MovimientoForm, MovimientoService> implements HasUrlParameter<Long> {

    private final CustomerService customerService;
    
    private ComboBox<Customer> clientFilter = new ComboBox<>();

    public MovimientoListView(MovimientoService movementService, CustomerService customerService) {
        super(Movimiento.class, "Historial de Movimientos", movementService);
        this.customerService = customerService;
        
        // Lo configuramos aquí porque 'customerService' ya no es null
        clientFilter.setItems(customerService.findAll());
    }

    @Override
    protected void init() {
        super.init(); 
        // 2. Ahora 'this.form' YA EXISTE (no es null).
        form.setClientes(customerService.findAll());
    }

    @Override
    protected MovimientoForm createForm() {
        return new MovimientoForm(java.util.Collections.emptyList());
    }
    
    @Override
    public void updateList() {
        // Nota: super.updateList() usa el filterText, así que NO lo llamamos.
        Customer clienteSeleccionado = clientFilter.getValue();

        if (clienteSeleccionado == null) {
            grid.setItems(service.findAll(null));
        } else 
            grid.setItems(service.findAllByCliente(clienteSeleccionado.getId())); 

        if (customerService != null) {
        	java.util.List<Customer> clientes = customerService.findAll();
            this.form.setClientes(clientes);
            //this.clientFilter.setItems(clientes);
        }
    }

    @Override
    protected void configureGrid() {
        grid.setColumns("fecha", "descripcion");
        grid.addColumn(m -> m.getCliente().getName()).setHeader("Cliente");
        
        grid.addColumn(new ComponentRenderer<>(mov -> {
            Span span = new Span(String.format("$ %.2f", mov.getMonto()));
            if (mov.getTipo() == MovementType.DEUDA) {
                span.getElement().getThemeList().add("badge error");
                span.setText("- " + span.getText());
            } else {
                span.getElement().getThemeList().add("badge success");
                span.setText("+ " + span.getText());
            }
            return span;
        })).setHeader("Monto");

        grid.asSingleSelect().addValueChangeListener(event -> editItem(event.getValue(), true));
    }

    @Override
    protected Component getToolbar(String title) {
        clientFilter.setPlaceholder("Filtrar por Cliente...");
        clientFilter.setClearButtonVisible(true);
        clientFilter.setItemLabelGenerator(Customer::getName); 
        clientFilter.setWidth("250px");
        clientFilter.addValueChangeListener(e -> updateList());
        
        
        Button addButton = new Button("Nuevo Item");
        addButton.addClickListener(click -> addItem());

        return new ViewToolbar(title, ViewToolbar.group(clientFilter, addButton));
    }

    /**
     * Este método se ejecuta automáticamente al navegar a esta vista con un parámetro.
     * Ejemplo: UI.navigate(MovimientoListView.class, 123L);
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long customerId) {
        if (customerId != null) {
            Customer cliente = customerService.findById(customerId).orElse(null); 
            
            if (cliente != null && clientFilter != null) {
                // Al setear el valor, se disparará el listener que configuramos en el constructor,
                clientFilter.setValue(cliente);
            }
        } else {
            if (clientFilter != null) {
                clientFilter.clear();
            }
        }
    }
}