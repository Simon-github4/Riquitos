package com.riquitos.base.ui;

import com.riquitos.AbstractCrudService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;

import jakarta.annotation.PostConstruct;

public abstract class AbstractListView<T, F extends AbstractForm<T>, S extends AbstractCrudService<T, ?>> 
       extends VerticalLayout {

    protected Grid<T> grid;
    protected F form;
    protected S service; 
    protected TextField filterText = new TextField();
    
    private Class<T> entityClass;// Necesitamos la clase para poder hacer newInstance() sin errores

    public AbstractListView(Class<T> entityClass, String title, S service) {
        this.entityClass = entityClass;
        this.service = service; 
        
        addClassName("list-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);
        
        this.grid = new Grid<>(entityClass, false);
        this.grid.setHeightFull(); 
        this.form = createForm();
        
        configureGrid(); // Las columnas siguen siendo específicas de cada hijo
        configureFormLogic();
        
        Component content = getContent();
        add(getToolbar(title), content);
        setFlexGrow(1, content); 
        closeEditor();
    }
    
    @PostConstruct
    private void init() {
    	updateList();
    }
    
    // --- MÉTODOS ABSTRACTOS ---
    protected abstract F createForm();
    protected abstract void configureGrid(); 
    // ----------------------------------------------

    private void configureFormLogic() {
        form.setWidth("250px");
        //form.setMinWidth("400px"); 
        
        // GUARDAR y BORRAR: Usa el servicio genérico directamente
        form.addSaveListener( e -> {
            try {
            	service.save(e.getBean());
	            updateList();
	            closeEditor();
	            Notification.show("Guardado correctamente", 3000, Position.TOP_CENTER)
	            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
            	ex.printStackTrace();
                Notification.show("No se pudo guardar.", 5000, Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        form.addDeleteListener( e -> {
            try {
                service.delete(e.getBean());
                updateList();
                closeEditor();
                Notification.show("Elemento eliminado", 3000, Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            } catch (Exception ex) {// Captura errores de integridad referencial (ej: borrar una Marca usada en Productos)
                Notification.show("No se puede eliminar: el ítem está en uso.", 5000, Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        form.addCloseListener( e -> closeEditor());
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private Component getToolbar(String title) {
        filterText.setPlaceholder("Filtrar...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        // filterText.getStyle().setPaddingRight("8px"); // Ya no es necesario si usas ViewToolbar

        Button addButton = new Button("Nuevo Item");
        addButton.addClickListener(click -> addItem());

        return new ViewToolbar(title, ViewToolbar.group(filterText, addButton));
    }

    public void editItem(T item, boolean showDelete) {
        if (item == null) {
            closeEditor();
        } else {
            form.setBean(item); 
            form.setVisible(true);
            // Lógica opcional: Si es nuevo (ID nulo), ocultar botón borrar
            // Esto depende de si tu entidad tiene método getId(), pero por ahora lo dejamos simple
            form.showButtonDelete(showDelete); 
            addClassName("editing");
        }
    }

    private void addItem() {
        grid.asSingleSelect().clear();
        try {
            T newItem = entityClass.getDeclaredConstructor().newInstance();
            editItem(newItem, false);
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error interno creando nuevo ítem", 3000, Position.MIDDLE);
        }
    }

    private void closeEditor() {
        form.setBean(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    protected void updateList() {
        grid.setItems(service.findAll(filterText.getValue()));
    }
}