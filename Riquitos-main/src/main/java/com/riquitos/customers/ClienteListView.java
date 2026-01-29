package com.riquitos.customers;

import java.math.BigDecimal;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.movimientos.MovimientoListView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "customers", layout = MainLayout.class)
@PageTitle("Clientes | Riquitos")
@Menu(order = 4, icon = "vaadin:users", title = "Clientes")
@PermitAll
public class ClienteListView extends AbstractListView<Customer, ClienteForm, CustomerService> {

    public ClienteListView(CustomerService customerService) {
        super(Customer.class, "Lista de Clientes", customerService);
    }

    @Override
    protected ClienteForm createForm() {
        return new ClienteForm();
    }

    @Override
    protected void configureGrid() {
        grid.addClassName("cliente-grid");
        grid.setSizeFull();
        grid.addColumn(Customer::getName).setHeader("Nombre").setSortable(true);
        grid.addColumn(Customer::getEmail).setHeader("email");
        grid.addColumn(Customer::getPhone).setHeader("Telefono");

        // Columna de Saldo Personalizada
        grid.addColumn(new ComponentRenderer<>(cliente -> {
            BigDecimal saldo = cliente.getSaldoActual();
            Span span = new Span(String.format("$ %.2f", saldo));
            
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                span.getElement().getThemeList().add("badge error"); // Rojo (Debe)
            } else if (saldo.compareTo(BigDecimal.ZERO) < 0) {
                span.getElement().getThemeList().add("badge success"); // Verde (A favor)
            } else {
                span.getElement().getThemeList().add("badge contrast"); // Neutro
            }
            return span;
        })).setHeader("Saldo Actual").setSortable(true);

        // --- NUEVA COLUMNA: Botón para ir a Movimientos ---
        grid.addComponentColumn(cliente -> {
            Button btnVerMovimientos = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
            btnVerMovimientos.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            btnVerMovimientos.setTooltipText("Ver historial de movimientos");
            
            btnVerMovimientos.addClickListener(e -> {
                // Navegamos a MovimientoListView pasando el ID del cliente, Esto llamará al método setParameter que creamos en el Paso 1
                UI.getCurrent().navigate(MovimientoListView.class, cliente.getId());
            });
            
            return btnVerMovimientos;
        }).setHeader("Historial").setWidth("95px").setTextAlign(ColumnTextAlign.CENTER);
        
        // Al hacer click en una fila, editamos el item (Lógica del padre)
        grid.asSingleSelect().addValueChangeListener(event -> editItem(event.getValue(), true));
    }
}