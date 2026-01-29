package com.riquitos.dashboard;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.production.ScanView;
import com.riquitos.production.material.RawMaterial;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

@PageTitle("Dashboard Riquitos")
@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, icon = "vaadin:dashboard", title = "Dashboard Riquitos")
//@CssImport("./styles/dashboard.css")
@PermitAll
public class DashboardView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    
    private final DashboardService dashboardService;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat integerFormat = new DecimalFormat("#,##0");

    // UI Components
    private VerticalLayout kpisContainer;
    private Grid<DashboardService.ClienteDeudaDTO> alertasGrid;
    private Div refreshIndicator;

    public DashboardView(DashboardService dashboardService) {
        this.dashboardService = dashboardService;

        setupLayout();
        createKPIsSection();
        createClientesDeudoresSection();
        createAccesosRapidosSection();
        
        // Auto-refresh cada 30 segundos
        setupAutoRefresh();
    }

    @PostConstruct
    public void refresh() {
        updateKPIs();
        updateClientesDeudores();
    }

    private void setupLayout() {
    	//addClassName("page-view");
        setSpacing(false);
        setPadding(false);
        setWidthFull();

        // Header
        add(new ViewToolbar("Panel de Control"));
        
        refreshIndicator = new Div();
        refreshIndicator.setText("🔄 Actualizando...");
        refreshIndicator.setVisible(false);
        refreshIndicator.getStyle()
            .set("text-align", "center")
            //.set("padding", "10px")
            .set("font-style", "italic");
        add(refreshIndicator);
    }

    private void createKPIsSection() {
        H2 titulo = new H2("Indicadores Clave de Hoy");
        titulo.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Left.MEDIUM);
        add(titulo);

        kpisContainer = new VerticalLayout();
        kpisContainer.setWidthFull();
        kpisContainer.setSpacing(true);
        kpisContainer.setPadding(true);
        add(kpisContainer);

        updateKPIs();
    }

    private void updateKPIs() {
        kpisContainer.removeAll();

        // Fila 1: Producción y Stock
        HorizontalLayout fila1 = new HorizontalLayout();
        fila1.setWidthFull();
        fila1.setSpacing(true);

        fila1.add(createKPICard(
            "Producción Hoy", 
            integerFormat.format(dashboardService.getProduccionHoy()) + " paquetes",
            VaadinIcon.FACTORY,
            "#28a745"
        ));

        fila1.add(createKPICard(
            "Producción Semana", 
            integerFormat.format(dashboardService.getProduccionSemana()) + " paquetes",
            VaadinIcon.CHART_LINE,
            "#17a2b8"
        ));

        fila1.add(createKPICard(
            "Stock Total", 
            decimalFormat.format(dashboardService.getTotalStockDisponible()) + " kg/L",
            VaadinIcon.PACKAGE,
            "#6c757d"
        ));

        fila1.add(createKPICard(
            "Alertas Activas", 
            String.valueOf(dashboardService.getCountAlertas()),
            VaadinIcon.EXCLAMATION_CIRCLE,
            dashboardService.getCountAlertas() > 0 ? "#dc3545" : "#28a745"
        ));

        kpisContainer.add(fila1);

        // Fila 2: Finanzas
        HorizontalLayout fila2 = new HorizontalLayout();
        fila2.setWidthFull();
        fila2.setSpacing(true);

        fila2.add(createKPICard(
            "Deudas Hoy", 
            "$" + decimalFormat.format(dashboardService.getDeudasHoy()),
            VaadinIcon.ARROW_DOWN,
            "#dc3545"
        ));

        fila2.add(createKPICard(
            "Pagos Hoy", 
            "$" + decimalFormat.format(dashboardService.getPagosHoy()),
            VaadinIcon.ARROW_UP, 
            "#28a745"
        ));

        kpisContainer.add(fila2);
    }

    private Component createKPICard(String title, String value, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames("kpi-card");
        card.setWidthFull();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("border-left", "4px solid " + color);

        Icon iconComponent = icon.create();//VaadinIcon.valueOf(icon.substring(icon.indexOf(":") + 1).toUpperCase()).create();
        iconComponent.getStyle()
            .set("font-size", "24px")
            .set("color", color);

        Div titleDiv = new Div(title);
        titleDiv.getStyle()
            .set("font-size", "14px")
            .set("color", "#6c757d")
            .set("margin-bottom", "8px")
            .set("font-weight", "500");

        Div valueDiv = new Div(value);
        valueDiv.getStyle()
            .set("font-size", "28px")
            .set("font-weight", "bold")
            .set("color", "#212529")
            .set("line-height", "1.2");

        VerticalLayout content = new VerticalLayout(iconComponent, titleDiv, valueDiv);
        content.setSpacing(false);
        content.setPadding(false);
        content.setAlignItems(Alignment.START);

        card.add(content);
        return card;
    }

    private void createClientesDeudoresSection() {
        H3 titulo = new H3("Clientes con Más Deudas");
        titulo.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Left.MEDIUM);
        add(titulo);

        alertasGrid = new Grid<>();
        alertasGrid.addClassNames("deudores-grid", LumoUtility.Margin.Left.MEDIUM);
        alertasGrid.setWidthFull();
        alertasGrid.setHeight("300px");
        alertasGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        // Columnas
        alertasGrid.addColumn(DashboardService.ClienteDeudaDTO::getNombreCliente)
            .setHeader("Cliente")
            .setAutoWidth(true);
            //.setFlexGrow(1);

        alertasGrid.addColumn(deuda -> "$" + decimalFormat.format(deuda.getDeudaTotal()))
            .setHeader("Deuda Total")
            .setAutoWidth(true);

        alertasGrid.addColumn(deuda -> {
            String nivel = deuda.getNivelDeuda();
            if ("ALTO".equals(nivel)) {
                return "🔴 Alto";
            } else if ("MEDIO".equals(nivel)) {
                return "🟡 Medio";
            } else {
                return "🟢 Bajo";
            }
        })
            .setHeader("Nivel de Riesgo")
            .setWidth("250px")
            .setFlexGrow(0);

        updateClientesDeudores();
        add(alertasGrid);
    }

    private void updateClientesDeudores() {
        List<DashboardService.ClienteDeudaDTO> deudores = dashboardService.getTopClientesDeudores(10);
        alertasGrid.setItems(deudores);

        if (deudores.isEmpty()) {
            Notification notification = Notification.show("✅ No hay clientes con deudas");
            notification.setPosition(Notification.Position.TOP_CENTER);
            notification.setDuration(3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void createAccesosRapidosSection() {
        H3 titulo = new H3("Acceso Rápido");
        titulo.addClassNames(LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.SMALL, LumoUtility.Margin.Left.MEDIUM);
        add(titulo);

        HorizontalLayout accesosLayout = new HorizontalLayout();
        accesosLayout.setWidthFull();
        accesosLayout.setSpacing(true);
        accesosLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Botón principal: Escanear QR
        Button qrButton = new Button("Escanear QR Producción");
        qrButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        qrButton.setIcon(new Icon(VaadinIcon.QRCODE));
        qrButton.addClickListener(e -> {
            UI.getCurrent().navigate(ScanView.class);
        });
        qrButton.setWidth("250px");
        qrButton.setHeight("60px");

        // Botones secundarios
        Button stockButton = new Button("Gestionar Stock");
        stockButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        stockButton.setIcon(new Icon(VaadinIcon.PACKAGE));
        stockButton.addClickListener(e -> {
            Notification notification = Notification.show("Función de gestión de stock próximamente");
            notification.setDuration(3000);
        });

        Button produccionButton = new Button("Ver Producción");
        produccionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        produccionButton.setIcon(new Icon(VaadinIcon.CHART_LINE));
        produccionButton.addClickListener(e -> {
            Notification notification = Notification.show("Función de producción próximamente");
            notification.setDuration(3000);
        });

        Button finanzasButton = new Button("Ver Finanzas");
        finanzasButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        finanzasButton.setIcon(new Icon(VaadinIcon.DOLLAR));
        finanzasButton.addClickListener(e -> {
            Notification notification = Notification.show("Función de finanzas próximamente");
            notification.setDuration(3000);
        });

        VerticalLayout principalLayout = new VerticalLayout(qrButton);
        principalLayout.setAlignItems(Alignment.CENTER);
        principalLayout.setSpacing(false);

        HorizontalLayout secundariosLayout = new HorizontalLayout(stockButton, produccionButton, finanzasButton);
        secundariosLayout.setSpacing(true);
        secundariosLayout.setAlignItems(Alignment.CENTER);

        accesosLayout.add(principalLayout, secundariosLayout);
        add(accesosLayout);
    }

    private void setupAutoRefresh() {
    	UI.getCurrent().getPage().executeJs(
    	        "setInterval(function() {" +
    	        "   $0.refresh();" +  // Necesitas especificar el método a llamar
    	        "}, 30000);",  // 30000 ms = 30 segundos
    	        getElement()
    	    );
    }


}