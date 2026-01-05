package com.riquitos.base.ui;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public final class ViewToolbar extends Composite<HorizontalLayout> {

    public ViewToolbar(@Nullable String viewTitle, Component... components) {
        var layout = getContent();
        layout.setPadding(true);
        layout.setWrap(true);
        layout.setWidthFull();
        layout.addClassName(LumoUtility.Border.BOTTOM);
        // Alineamos todo verticalmente al centro para que quede prolijo
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(LumoUtility.Margin.NONE);

        var title = new H1(viewTitle);
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE, LumoUtility.FontWeight.LIGHT);

        var toggleAndTitle = new HorizontalLayout(drawerToggle, title);
        toggleAndTitle.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.add(toggleAndTitle);

        // --- CAMBIO AQUÍ ---
        // Eliminamos layout.setFlexGrow(1, toggleAndTitle); para que no empuje todo a la derecha.

        if (components.length > 0) {
            var actions = new HorizontalLayout(components);
            
            // Opcional: Agregamos un margen a la izquierda para separarlo del título
            actions.addClassName(LumoUtility.Margin.Left.LARGE); 
            
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            layout.add(actions);
            
            // Si quisieras que la barra de búsqueda se estire para llenar el hueco, 
            // descomenta la siguiente línea:
            // layout.setFlexGrow(1, actions); 
        }
    }

    public static Component group(Component... components) {
        var group = new HorizontalLayout(components);
        group.setWrap(true);
        // Aseguramos alineación interna
        group.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return group;
    }
}