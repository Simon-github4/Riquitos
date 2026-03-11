package com.riquitos.product;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
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
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

public class ProductForm extends AbstractForm<Product> {

    // Coinciden con la entidad para el bind automático
    TextField sku = new TextField("SKU / Código");
    TextField description = new TextField("Descripción");
    BigDecimalField costPrice = new BigDecimalField("Precio Costo");
    IntegerField unitiesPerBagOrBox = new IntegerField("Unidades x Bolson/Caja");
    IntegerField netWeight = new IntegerField("Peso en Gramos (g)", "Ej: 250 para 250g");

    // --- CAMPO PARA IMAGEN ---
    private final MemoryBuffer imageBuffer = new MemoryBuffer();
    private final Upload imageUpload = new Upload(imageBuffer);
    private final Image previewImage = new Image();
    private byte[] currentImageData;

    // --- CAMPOS PARA GESTIÓN DE RECETA (No se bindean automático) ---
    private final Grid<ProductIngredient> recipeGrid = new Grid<>(ProductIngredient.class, false);
    private final ComboBox<RawMaterial> rawMaterialCombo = new ComboBox<>("Insumo");
    private final BigDecimalField quantityField = new BigDecimalField("Cantidad x Kg de Producto");
    private final Button addIngredientBtn = new Button(VaadinIcon.PLUS.create());

    private final RawMaterialService rawMaterialService;

    public ProductForm(RawMaterialService rawMaterialService) {
        super(Product.class);
        this.rawMaterialService = rawMaterialService;
        
        costPrice.setPrefixComponent(new Span("$")); 
        costPrice.setLocale(new Locale("es", "AR")); 
        costPrice.setPlaceholder("0.00");
        
        configureImageUpload();
        configureRecipeControls();
        configureRecipeGrid();

        binder.bindInstanceFields(this);
        
        VerticalLayout imageSection = new VerticalLayout();
        imageSection.setPadding(false);
        imageSection.setSpacing(false);
        imageSection.add(createImageLayout());

        VerticalLayout recipeSection = new VerticalLayout();
        recipeSection.setPadding(false);
        recipeSection.setSpacing(false);
        recipeSection.add(
            new H4("Receta"),
            createIngredientInputLayout(), 
            recipeGrid                    
        );
        
        HorizontalLayout rowContainer = new HorizontalLayout(sku, unitiesPerBagOrBox, netWeight);
        rowContainer.setSpacing(true);
        rowContainer.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        sku.setMinWidth("0");
        unitiesPerBagOrBox.setMinWidth("0");
        (rowContainer.getComponentAt(2)).getStyle().set("min-width", "0");
        
        add(rowContainer);
        addFormRow(description, costPrice);
        add(imageSection, recipeSection);
        
        setColspan(rowContainer, 2);
        setColspan(imageSection, 2); 
        setColspan(recipeSection, 2); 
        
    }

    // --- CONFIGURACIÓN UI DE IMAGEN ---
    private void configureImageUpload() {
    	imageUpload.setUploadButton(new Button(VaadinIcon.UPLOAD.create()));
        imageUpload.setAcceptedFileTypes("image/*");
        imageUpload.setDropLabel(new Span("Arrastra una imagen aquí o haz clic para seleccionar"));
        imageUpload.setMaxFileSize(5 * 1024 * 1024); // 5MB max
        imageUpload.setWidthFull();

        previewImage.setWidth("150px");
        previewImage.setHeight("100px");
        previewImage.getStyle().set("object-fit", "cover");
        previewImage.getStyle().set("border-radius", "8px");
        previewImage.getStyle().set("border", "1px solid #e0e0e0");

        imageUpload.addSucceededListener(event -> {
            try {
                InputStream inputStream = imageBuffer.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                currentImageData = outputStream.toByteArray();
                
                if (getBean() != null) 
                    getBean().setImageData(currentImageData);
                    
                previewImage.setSrc("data:image/png;base64," + 
                    java.util.Base64.getEncoder().encodeToString(currentImageData));
                
                Notification.show("Imagen cargada correctamente", 2000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
            } catch (Exception e) {
                Notification.show("Error al cargar la imagen: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        imageUpload.addFailedListener(event -> {
            Notification.show("Error al cargar archivo: " + event.getReason().getMessage(), 
                3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    private HorizontalLayout createImageLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.setAlignItems(Alignment.CENTER);
        
        VerticalLayout uploadLayout = new VerticalLayout();
        uploadLayout.setSpacing(false);
        uploadLayout.setPadding(false);
        uploadLayout.add(imageUpload);
        uploadLayout.setWidth("100%");
        
        VerticalLayout previewLayout = new VerticalLayout();
        previewLayout.setSpacing(false);
        previewLayout.setPadding(false);
        previewLayout.setAlignItems(Alignment.CENTER);
        previewLayout.add(new Span("Vista previa:"), previewImage);
        previewLayout.setWidth("60%");
        
        layout.add(uploadLayout, previewLayout);
        return layout;
    }

    // --- CONFIGURACIÓN UI DE RECETA ---
    private void configureRecipeControls() {
        // Llenamos el combo con los insumos de la base de datos
        rawMaterialCombo.setItems(rawMaterialService.findAll());
        rawMaterialCombo.setItemLabelGenerator(RawMaterial::getName);
        rawMaterialCombo.setPlaceholder("Seleccionar insumo...");
        rawMaterialCombo.setWidth("50%");

        quantityField.setPlaceholder("Ej: 0.25");
        quantityField.setWidth("30%");
        quantityField.setHelperText("( x Kg de Producto)");
        quantityField.setTooltipText("Cantidad x KG de producto terminado");

        addIngredientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIngredientBtn.addClickListener(e -> addIngredient());
    }

    private void configureRecipeGrid() {
        recipeGrid.addClassName("recipe-grid");
        recipeGrid.setHeight("180px");
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
    // Esto es crucial: Cuando editamos un producto, debemos cargar su receta e imagen en el Grid
    @Override
    public void setBean(Product bean) {
        super.setBean(bean); // El binder carga los campos básicos
        
        // Si la lista es null (producto nuevo), la inicializamos
        if (bean != null && bean.getRecipe() == null) {
            bean.setRecipe(new ArrayList<>());
        }
        
        imageUpload.getElement().executeJs("this.files=[]");
        
        // Cargar imagen existente si hay
        if (bean != null && bean.getImageData() != null && bean.getImageData().length > 0) {
            currentImageData = bean.getImageData();
            previewImage.setSrc("data:image/png;base64," + Base64.getEncoder().encodeToString(bean.getImageData()));
        } else {
            currentImageData = null;
            previewImage.setSrc("");
        }
        
        refreshRecipeGrid();
    }

    @Override
    public Product getBean() {
        Product bean = super.getBean();
        if (bean != null && currentImageData != null) {
            bean.setImageData(currentImageData);
        }
        return bean;
    }
}