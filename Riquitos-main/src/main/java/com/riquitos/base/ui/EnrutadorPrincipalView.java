package com.riquitos.base.ui;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.riquitos.dashboard.DashboardView;
import com.riquitos.production.ProductSelectionView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route("") // Esta es la ruta por defecto tras el login
@PermitAll // Asegura que solo usuarios autenticados lleguen aquí
public class EnrutadorPrincipalView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            // Verificamos si tiene el rol de OPERARIO
            // Spring Security agrega automáticamente el prefijo "ROLE_"
            boolean esOperario = auth.getAuthorities().stream()
                    .anyMatch(rol -> rol.getAuthority().equals("ROLE_OPERARIO"));

            boolean esVendedor = auth.getAuthorities().stream()
                    .anyMatch(rol -> rol.getAuthority().equals("ROLE_VENDEDOR"));

            if (esOperario) {
                // Lo mandamos directo al módulo de producción (ej: control de lotes de snacks/papas)
                event.forwardTo(ProductSelectionView.class); 
            } else if (esVendedor) {
                // Lo mandamos al módulo de gestión/ventas
                event.forwardTo(ProductSelectionView.class);
            } else {
                // Si es ADMIN o cualquier otro, a un panel general
                event.forwardTo(DashboardView.class);
            }
        }
    }
}