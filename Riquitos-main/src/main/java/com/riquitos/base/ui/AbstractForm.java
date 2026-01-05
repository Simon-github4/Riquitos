package com.riquitos.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

// T es el tipo de entidad (ej: Marca, Cliente, Producto)
public abstract class AbstractForm<T> extends FormLayout {

    private T bean;
    protected Binder<T> binder;

    protected Button save = new Button("Ok");
    protected Button delete = new Button("Borrar");
    protected Button close = new Button("Cancelar");

    public AbstractForm(Class<T> beanType) {
        addClassName("form-view");
        
        // Inicializamos el Binder con la clase genérica
        this.binder = new BeanValidationBinder<>(beanType);

        // Configuración de botones base
        configureButtons();
        
        // Listener para habilitar/deshabilitar guardar según validación
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
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

    public Component createButtonsLayout() {
        return new HorizontalLayout(save, delete, close);
    }
    
    public void showButtonDelete(boolean show) {
        delete.setVisible(show);
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
