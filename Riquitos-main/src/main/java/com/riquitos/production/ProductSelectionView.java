package com.riquitos.production;

import java.math.BigDecimal;
import java.util.List;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.riquitos.product.Product;
import com.riquitos.product.ProductService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PageTitle("Selección de Productos")
@Route(value = "select-products", layout = MainLayout.class)
@Menu(order = 4, icon = "vaadin:grid", title = "Producción por Selección")
@PermitAll
public class ProductSelectionView extends VerticalLayout {

    private final ProductService productService;
    private final ProductionBatchService productionBatchService;
    
    private Product selectedProduct;
    private Div productsContainer;

    public ProductSelectionView(ProductService productService, ProductionBatchService productionBatchService) {
        this.productService = productService;
        this.productionBatchService = productionBatchService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        add(new ViewToolbar("Producción por Selección de Productos"));
        
        // Contenedor para los productos
        productsContainer = new Div();
        productsContainer.setWidthFull();
        productsContainer.getStyle().set("display", "grid");
        productsContainer.getStyle().set("gap", "20px");
        productsContainer.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))");
        productsContainer.getStyle().set("padding", "16px");
        
        // Botón de refresh
        Button refreshBtn = new Button("Actualizar Productos", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> loadProducts());
        
        add(refreshBtn, productsContainer);
        
        loadProducts();
    }

    private void loadProducts() {
        productsContainer.removeAll();
        
        List<Product> products = productService.findAllByDescAsc();
        
        for (Product product : products) {
            Div productCard = createProductCard(product);
            productsContainer.add(productCard);
        }
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.getStyle().set("width", "auto");
        card.setMinHeight("260px");
        card.getStyle().set("border", "2px solid #e0e0e0");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("padding", "12px");
        card.getStyle().set("cursor", "pointer");
        card.getStyle().set("transition", "all 0.3s ease");
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        card.getStyle().set("display", "flex");
        card.getStyle().set("flex-direction", "column");
        card.getStyle().set("align-items", "center");
        card.getStyle().set("justify-content", "center");
        card.getStyle().set("text-align", "center");
        
        card.getStyle().set("box-sizing", "border-box"); // Fundamental para que el padding no "infle" la card
        
        // Efectos hover
        card.getElement().addEventListener("mouseover", e -> {
            card.getStyle().set("border-color", "#007bff");
            card.getStyle().set("transform", "translateY(-2px)");
            card.getStyle().set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)");
        });
        
        card.getElement().addEventListener("mouseout", e -> {
            card.getStyle().set("border-color", "#e0e0e0");
            card.getStyle().set("transform", "translateY(0)");
            card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        });

        // Imagen del producto
        Image productImage = new Image();
        if (product.getImageData() != null && product.getImageData().length > 0) {
            // Mostrar imagen real desde BLOB
            String base64Image = java.util.Base64.getEncoder().encodeToString(product.getImageData());
            productImage.setSrc("data:image/png;base64," + base64Image);
        } else {
            // Placeholder si no hay imagen
            String initials = product.getDescription().length() >= 3 ? 
                product.getDescription().substring(0, 3).toUpperCase() : 
                product.getDescription().toUpperCase();
            productImage.setSrc("https://via.placeholder.com/120x120/007bff/ffffff?text=" + initials);
        }
        productImage.setWidth("100%"); 
        productImage.setMaxWidth("180px"); 
        productImage.setHeight("180px"); 
        productImage.getStyle().set("border-radius", "8px");
        productImage.getStyle().set("margin-bottom", "12px"); 
        productImage.getStyle().set("object-fit", "cover"); 

        // Nombre del producto
        Span nameSpan = new Span(product.getDescription());
        nameSpan.getStyle().set("font-weight", "bold");
        nameSpan.getStyle().set("font-size", "14px");
        nameSpan.getStyle().set("margin-bottom", "4px");
        nameSpan.getStyle().set("word-break", "break-word");
        
        // SKU si existe
        Span skuSpan = new Span();
        if (product.getSku() != null && !product.getSku().isEmpty()) {
            skuSpan.setText("SKU: " + product.getSku());
            skuSpan.getStyle().set("font-size", "12px");
            skuSpan.getStyle().set("color", "#666");
        }

        VerticalLayout cardContent = new VerticalLayout(productImage, nameSpan);
        cardContent.setAlignItems(Alignment.CENTER);
        cardContent.setSpacing(false);
        cardContent.setPadding(false);
        cardContent.setWidth("100%");
        
        if (product.getSku() != null && !product.getSku().isEmpty()) {
            cardContent.add(skuSpan);
        }

        card.add(cardContent);
        
        card.getStyle().set("height", "auto"); 
        card.getStyle().set("min-height", "280px");
        
        // Click handler
        card.addClickListener(e -> selectProduct(product));
        
        return card;
    }

    private void selectProduct(Product product) {
        this.selectedProduct = product;
        openQuantityDialog(product);    }



    private void openQuantityDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registrar Producción");
        dialog.setMinWidth("20em");
        
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        
        Div productInfo = new Div();
        productInfo.setText("Producto: " + product.getDescription());
        productInfo.getStyle().set("font-weight", "bold");
        
        BigDecimalField quantityField = new BigDecimalField("Bolsones / Cajas Producidas");
        quantityField.setPlaceholder("0.00");
        quantityField.setWidthFull();
        quantityField.setRequired(true);
        
        Button confirmBtn = new Button("Confirmar", e -> {
            if (quantityField.getValue() == null || quantityField.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                Notification.show("Ingrese una cantidad válida", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            registerProduction(product, quantityField.getValue());
            dialog.close();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        confirmBtn.addClickShortcut(com.vaadin.flow.component.Key.ENTER);
        confirmBtn.setDisableOnClick(true);
        
        Button cancelBtn = new Button("Cancelar", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmBtn, cancelBtn);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setWidthFull();
        
        layout.add(productInfo, quantityField, buttonLayout);
        dialog.add(layout);
        
        dialog.open();
        quantityField.focus();
    }

    private void registerProduction(Product product, BigDecimal quantity) {
        try {
            productionBatchService.registrarProduccion(product, quantity);
            
            Notification.show(
                "Producción registrada: " + product.getDescription() + " x " + quantity, 
                3000, 
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
        } catch (Exception e) {
            Notification.show(
                "Error al registrar producción: " + e.getMessage(), 
                5000, 
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}