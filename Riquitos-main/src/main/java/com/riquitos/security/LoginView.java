package com.riquitos.security;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login") 
@PageTitle("Ingreso | Riquitos")
@AnonymousAllowed 
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull(); 
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        

        login.setAction("login"); // Spring Security espera esta acción por defecto
        login.setForgotPasswordButtonVisible(false);
        login.setI18n(createSpanishI18n());
        
        add(new H1("Riquitos"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Si hay un error (contraseña mal), mostramos el mensaje rojo del componente
        if(beforeEnterEvent.getLocation()
            .getQueryParameters()
            .getParameters()
            .containsKey("error")) {
            login.setError(true);
        }
    }
    
    private LoginI18n createSpanishI18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        // 1. Traducir etiquetas del formulario
        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Iniciar Sesión");
        form.setUsername("Usuario");
        form.setPassword("Contraseña");
        form.setSubmit("Ingresar");
        // form.setForgotPassword("¿Olvidaste tu contraseña?"); // (Ya lo ocultaste, pero por si acaso)
        i18n.setForm(form);

        // 2. Traducir mensajes de error
        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Usuario o contraseña incorrectos");
        errorMessage.setMessage("Por favor, verifique que el usuario y la contraseña sean correctos e intente nuevamente.");
        i18n.setErrorMessage(errorMessage);

        return i18n;
    }
}