package com.riquitos.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

// T es el tipo de entidad (ej: Marca, Cliente, Producto)
public abstract class AbstractForm<T> extends VerticalLayout {

    private T bean;
    protected Binder<T> binder;
    private final FormLayout internalFormLayout = new FormLayout();
    
    protected Button save = new Button("Ok");
    protected Button delete = new Button("Borrar");
    protected Button close = new Button("Cancelar");

    public AbstractForm(Class<T> beanType) {
        addClassName("form-view");
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        
        this.binder = new BeanValidationBinder<>(beanType);
        configureButtons();
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));        
        
        internalFormLayout.setExpandFields(true);
        internalFormLayout.setAutoResponsive(true);
        internalFormLayout.setExpandColumns(true);

        HorizontalLayout footer = createButtonsLayout();
        footer.setWidthFull();
        footer.setPadding(true);
        footer.setAlignItems(Alignment.CENTER); // Centrar botones verticalmente en ese espacio grande
        footer.getElement().getStyle().set("flex-shrink", "0"); 
        
        super.add(internalFormLayout, footer);
    }

    private void configureButtons() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, bean)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
    }

    private void validateAndSave() {
        try {
            if (bean != null) {
                binder.writeBean(bean);
                fireEvent(new SaveEvent(this, bean));
            }
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public void setBean(T bean) {
        this.bean = bean;
        binder.readBean(bean);
    }
    
    public T getBean() {
        return bean;
    }

    public HorizontalLayout createButtonsLayout() {
        return new HorizontalLayout(save, delete, close);
    }
    
    public void showButtonDelete(boolean show) {
        delete.setVisible(show);
    }

    @Override
    public void add(Component... components) {
        internalFormLayout.add(components);
    }

    @Override
    public void remove(Component... components) {
        internalFormLayout.remove(components);
    }

    public void setColspan(Component component, int colspan) {
        internalFormLayout.setColspan(component, colspan);
    }

    public void addFormRow(Component... components) {
        internalFormLayout.addFormRow(components);
    }
    
    // --- EVENTOS GENÉRICOS ---
    public static abstract class AbstractFormEvent<T> extends ComponentEvent<AbstractForm<T>> {
        private T bean;

        protected AbstractFormEvent(AbstractForm<T> source, T bean) {
            super(source, false);
            this.bean = bean;
        }

        public T getBean() {
            return bean;
        }
    }

    public static class SaveEvent<T> extends AbstractFormEvent<T> {
        SaveEvent(AbstractForm<T> source, T bean) {
            super(source, bean);
        }
    }

    public static class DeleteEvent<T> extends AbstractFormEvent<T> {
        DeleteEvent(AbstractForm<T> source, T bean) {
            super(source, bean);
        }
    }

    public static class CloseEvent<T> extends AbstractFormEvent<T> {
        CloseEvent(AbstractForm<T> source) {
            super(source, null);
        }
    }

    /*public <E extends ComponentEvent<?>> Registration addListener(
    		Class<E> eventType,
            ComponentEventListener<E> listener
    ) {
        return getEventBus().addListener(eventType, listener);
    }*/
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addSaveListener(ComponentEventListener<SaveEvent<T>> listener) {
        return addListener(SaveEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addDeleteListener(ComponentEventListener<DeleteEvent<T>> listener) {
        return addListener(DeleteEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Registration addCloseListener(ComponentEventListener<CloseEvent<T>> listener) {
        return addListener(CloseEvent.class, (ComponentEventListener) listener);
    }
}
