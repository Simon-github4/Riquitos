package com.riquitos.pricelist;

import java.util.Locale;

import com.riquitos.base.ui.AbstractListView;
import com.riquitos.base.ui.MainLayout;
import com.riquitos.entities.PriceList;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "listas", layout = MainLayout.class)
@PageTitle("Listas")
@Menu(order = 1, icon = "vaadin:clipboard-check", title = "Listas")
@RolesAllowed({"VENDEDOR", "ADMIN"})
public class PriceListView extends AbstractListView<PriceList, PriceListForm, PriceListService>{

	public PriceListView(PriceListService service) {
		super(PriceList.class, "Listas Precios", service);
	}

	@PostConstruct
    private void init() {
        updateList(); // Llenamos la grilla al iniciar
    }
	
	@Override
	protected PriceListForm createForm() {
		return new PriceListForm();
	}

	@Override
	protected void configureGrid() {
		grid.addClassNames("Marca-grid");
		grid.addColumn(PriceList::getName).setHeader("Nombre");
		grid.addColumn(new NumberRenderer<>(
		        PriceList::getMarginPercentage,   // 1. El getter del valor
		        "%.2f %%",                        // 2. Formato: 2 decimales y símbolo %
		        new Locale("es", "AR")            // 3. Locale: Fuerza la coma decimal
		    ))
		    .setHeader("Margen");
		    //.setTextAlign(ColumnTextAlign.END);       
		grid.getColumns().forEach(col -> col.setAutoWidth(true));
        
        // Listener de selección
        grid.asSingleSelect().addValueChangeListener(event ->
            editItem(event.getValue(), true));		
	}


}
