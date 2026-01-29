package com.riquitos.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

import com.riquitos.base.ui.AbstractForm;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

public class ProductForm extends AbstractForm<Product> {

    // Coinciden con la entidad para el bind automático
    TextField sku = new TextField("SKU / Código");
    TextField description = new TextField("Descripción");
    BigDecimalField costPrice = new BigDecimalField("Precio Costo");

    // --- CAMPOS PARA GESTIÓN DE RECETA (No se bindean automático) ---
    private final Grid<ProductIngredient> recipeGrid = new Grid<>(ProductIngredient.class, false);
    private final ComboBox<RawMaterial> rawMaterialCombo = new ComboBox<>("Insumo");
    private final BigDecimalField quantityField = new BigDecimalField("Cantidad");
    private final Button addIngredientBtn = new Button(VaadinIcon.PLUS.create());

    private final RawMaterialService rawMaterialService;

    public ProductForm(RawMaterialService rawMaterialService) {
        super(Product.class);
        this.rawMaterialService = rawMaterialService;

        sku.setWidthFull();
        
        costPrice.setPrefixComponent(new Span("$")); 
        costPrice.setLocale(new Locale("es", "AR")); 
        costPrice.setPlaceholder("0,00");

        configureRecipeControls();
        configureRecipeGrid();

        binder.bindInstanceFields(this);

        VerticalLayout recipeSection = new VerticalLayout();
        recipeSection.setPadding(false);
        recipeSection.setSpacing(false);
        recipeSection.add(
            new H4("Receta"),
            createIngredientInputLayout(), 
            recipeGrid                    
        );

        add(
            sku, 
            description, 
            costPrice, 
            recipeSection, 
            createButtonsLayout()
        );
        // ocupe todo el ancho (2 columnas), para que no se ponga al costado de nada.
        setColspan(recipeSection, 2); 
        setColspan(sku, 2);
    }

    // --- CONFIGURACIÓN UI DE RECETA ---
    private void configureRecipeControls() {
        // Llenamos el combo con los insumos de la base de datos
        rawMaterialCombo.setItems(rawMaterialService.findAll());
        rawMaterialCombo.setItemLabelGenerator(RawMaterial::getName);
        rawMaterialCombo.setPlaceholder("Seleccionar insumo...");
        rawMaterialCombo.setWidth("50%");

        quantityField.setPlaceholder("Ej: 0,200");
        quantityField.setWidth("30%");

        addIngredientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIngredientBtn.addClickListener(e -> addIngredient());
    }

    private void configureRecipeGrid() {
        recipeGrid.addClassName("recipe-grid");
        recipeGrid.setHeight("200px"); // Altura fija para la sub-tabla
        recipeGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        recipeGrid.addColumn(pi -> pi.getRawMaterial().getName()).setHeader("Insumo");
        recipeGrid.addColumn(pi -> pi.getRawMaterial().getUnit()).setHeader("Unidad");
        recipeGrid.addColumn(ProductIngredient::getQuantityRequired).setHeader("Cant. X Unidad");
        
        // Botón Borrar en cada fila
        recipeGrid.addComponentColumn(ingredient -> {
            Button removeBtn = new Button(VaadinIcon.TRASH.create());
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            removeBtn.addClickListener(e -> removeIngredient(ingredient));
            return removeBtn;
        }).setHeader("Elim.");
    }

    private HorizontalLayout createIngredientInputLayout() {
        HorizontalLayout layout = new HorizontalLayout(rawMaterialCombo, quantityField, addIngredientBtn);
        layout.setWidthFull();
        layout.setAlignItems(Alignment.BASELINE); // Alinear visualmente los inputs y el botón
        return layout;
    }

    // --- LÓGICA DE GESTIÓN DE LISTA (Master-Detail) ---
    private void addIngredient() {
        RawMaterial material = rawMaterialCombo.getValue();
        BigDecimal qty = quantityField.getValue();

        if (material != null && qty != null) {
            ProductIngredient newItem = new ProductIngredient();
            newItem.setRawMaterial(material);
            newItem.setQuantityRequired(qty);
            newItem.setProduct(this.getBean()); // Importante: Vincular al padre

            // Agregamos a la lista del objeto en memoria
            this.getBean().getRecipe().add(newItem);
            
            // Refrescar UI
            refreshRecipeGrid();
            
            // Limpiar inputs
            quantityField.clear();
            rawMaterialCombo.clear();
            rawMaterialCombo.focus();
        }
    }

    private void removeIngredient(ProductIngredient ingredient) {
        this.getBean().getRecipe().remove(ingredient);
        refreshRecipeGrid();
    }

    private void refreshRecipeGrid() {
        if (getBean() != null && getBean().getRecipe() != null) {
            recipeGrid.setItems(getBean().getRecipe());
        } else {
            recipeGrid.setItems(new ArrayList<>());
        }
    }

    // --- SOBRESCRIBIR SETBEAN ---
    // Esto es crucial: Cuando editamos un producto, debemos cargar su receta en el Grid
    @Override
    public void setBean(Product bean) {
        super.setBean(bean); // El binder carga los campos básicos
        
        // Si la lista es null (producto nuevo), la inicializamos
        if (bean != null && bean.getRecipe() == null) {
            bean.setRecipe(new ArrayList<>());
        }
        
        // Actualizamos la grilla de ingredientes visualmente
        refreshRecipeGrid();
    }
}