package com.riquitos.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;          // Importado
import com.vaadin.flow.component.button.ButtonVariant;   // Importado
import com.vaadin.flow.component.html.Footer;            // Importado
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext; // Importado
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

//@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    private final AuthenticationContext authContext; // 1. Dependencia necesaria

    // 2. Inyectamos AuthenticationContext en el constructor
    MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        
        setPrimarySection(Section.DRAWER);
        // 3. Agregamos createFooter() al final
        addToDrawer(createHeader(), new Scroller(createSideNav()), createFooter());
    }

    private Component createHeader() {
        // TODO Replace with real application logo and name
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.setSize("48px");
        appLogo.setColor("green");

        var appName = new Span("Riquitos");
        appName.getStyle().setFontWeight(Style.FontWeight.BOLD);

        var header = new VerticalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    /*private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }*/
    private SideNav createSideNav() {
        SideNav nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        Icon icon = VaadinIcon.FACTORY.create();
        icon.addClassName(LumoUtility.TextColor.PRIMARY); // Usa el color primario del tema
        
        SideNavItem seccionProduccion = new SideNavItem("Producción");
        seccionProduccion.setPrefixComponent(icon); // Ícono de la carpeta

        icon = VaadinIcon.BRIEFCASE.create();
        icon.addClassName(LumoUtility.TextColor.PRIMARY); // Usa el color primario del tema

        SideNavItem seccionComercial = new SideNavItem("Comercial");
        seccionComercial.setPrefixComponent(icon);

        icon = VaadinIcon.STORAGE.create();
        icon.addClassName(LumoUtility.TextColor.PRIMARY); // Usa el color primario del tema
        
        SideNavItem seccionInventario = new SideNavItem("Inventario");
        seccionInventario.setPrefixComponent(icon);

        MenuConfiguration.getMenuEntries().forEach(entry -> {
            SideNavItem item = createSideNavItem(entry);
            String titulo = entry.title();

            if (titulo.equals("Lotes de Producción") || 
                titulo.startsWith("Producción por")) {
                
                seccionProduccion.addItem(item); // Agrega dentro de Producción
                
            } else if (titulo.equals("Clientes") || 
                       titulo.equals("Listas de precios") || 
                       titulo.equals("Ingresos/Egresos")) {
                
                seccionComercial.addItem(item); // Agrega dentro de Comercial
                
            } else if (titulo.equals("Insumos") || 
                       titulo.equals("Kardex de Stock")||
                       titulo.equals("Productos")) {
                       
                seccionInventario.addItem(item); // Agrega dentro de Inventario
                
            } else {
                // Si no coincide con ninguno, va a la raíz (ej: Dashboard, QR)
            	Component icon2 = item.getPrefixComponent();
                if (icon2 != null) 
                    icon2.addClassName(LumoUtility.TextColor.PRIMARY);
                nav.addItem(item);
           }
        });

        if (seccionInventario.getItems().size() > 0) nav.addItem(seccionInventario);
        if (seccionProduccion.getItems().size() > 0) nav.addItem(seccionProduccion);
        if (seccionComercial.getItems().size() > 0) nav.addItem(seccionComercial);

        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    // 4. Método nuevo para crear el botón de salir
    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames(LumoUtility.Padding.MEDIUM);

        Button logoutButton = new Button("Cerrar Sesión", event -> authContext.logout());
        logoutButton.setIcon(VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.setWidthFull();

        layout.add(logoutButton);
        return layout;
    }
}