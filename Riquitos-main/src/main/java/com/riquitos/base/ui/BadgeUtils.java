package com.riquitos.base.ui;

import com.vaadin.flow.component.html.Span;

public class BadgeUtils {

    public static void setWarningTheme(Span badge, boolean primary) {
        // Aseguramos que tenga la forma de badge
        badge.getElement().getThemeList().add("badge");

        if (primary) {
            // Estilo Sólido
            badge.getStyle().set("background-color", "#ffc107");
            badge.getStyle().set("color", "black");
        } else {
            // Estilo Suave
            badge.getStyle().set("background-color", "#fff1b8"); 
            badge.getStyle().set("color", "#5e4b05");
        }
    }
}
