package com.riquitos.reports;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PageTitle("Reportes")
@Route(value = "reports", layout = MainLayout.class)
@Menu(order = 10, icon = "vaadin:chart", title = "Reportes")
@PermitAll
public class ReportsView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService;

    private ComboBox<String> reportTypeCombo;
    private ComboBox<Integer> yearCombo;
    private ComboBox<Integer> monthCombo;
    private Button generatePdfButton;

    public ReportsView(ReportService reportService) {
        this.reportService = reportService;

        setupLayout();
        createFilters();
        createGenerateButton();
    }

    private void setupLayout() {
        setSpacing(false);
        setPadding(true);
        setWidthFull();
        add(new ViewToolbar("Generar Reportes PDF"));
    }

    private void createFilters() {
        HorizontalLayout filtersLayout = new HorizontalLayout();
        filtersLayout.setSpacing(true);
        filtersLayout.setAlignItems(Alignment.END);

        reportTypeCombo = new ComboBox<>("Tipo de Reporte");
        reportTypeCombo.setItems("Mensual", "Anual");
        reportTypeCombo.setValue("Mensual");
        reportTypeCombo.setWidth("200px");
        reportTypeCombo.addValueChangeListener(e -> {
            updateMonthVisibility();
        });

        int currentYear = LocalDate.now().getYear();
        yearCombo = new ComboBox<>("Año");
        yearCombo.setItems(Arrays.asList(
            currentYear, currentYear - 1, currentYear - 2, currentYear - 3, currentYear - 4
        ));
        yearCombo.setValue(currentYear);
        yearCombo.setWidth("120px");

        monthCombo = new ComboBox<>("Mes");
        monthCombo.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthCombo.setValue(LocalDate.now().getMonthValue());
        monthCombo.setWidth("120px");
        monthCombo.setItemLabelGenerator(this::getMonthName);

        filtersLayout.add(reportTypeCombo, yearCombo, monthCombo);
        add(filtersLayout);
    }

    private void createGenerateButton() {
        generatePdfButton = new Button("Generar PDF");
        generatePdfButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generatePdfButton.addClickListener(e -> generatePdf());
        add(generatePdfButton);
    }

    private void updateMonthVisibility() {
        boolean isMonthly = "Mensual".equals(reportTypeCombo.getValue());
        monthCombo.setVisible(isMonthly);
    }

	private void generatePdf() {
		try {
			String reportType = reportTypeCombo.getValue();
			Integer year = yearCombo.getValue(); // Cambiado a Integer por si es null

			if (year == null || reportType == null) 
				return;

			byte[] pdfBytes;
			String fileName;

			if ("Mensual".equals(reportType)) {
				Integer month = monthCombo.getValue();
				if (month == null) 
					return;
				pdfBytes = reportService.generateMonthlyReport(year, month);
				fileName = "reporte_" + getMonthName(month) + "_" + year + ".pdf";
			} else {
				pdfBytes = reportService.generateYearlyReport(year);
				fileName = "reporte_anual_" + year + ".pdf";
			}

			// 1. Convertir el byte[] a String Base64 en Java
			String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

			// 2. Ejecutar JS decodificando el Base64
			UI.getCurrent().getPage().executeJs("const byteCharacters = atob($0); " + // Decodificar Base64
					"const byteNumbers = new Array(byteCharacters.length); "
					+ "for (let i = 0; i < byteCharacters.length; i++) { "
					+ "    byteNumbers[i] = byteCharacters.charCodeAt(i); " + "} "
					+ "const byteArray = new Uint8Array(byteNumbers); " + // Convertir a array binario real
					"const blob = new Blob([byteArray], { type: 'application/pdf' }); " +
					"const url = window.URL.createObjectURL(blob); " + "const a = document.createElement('a'); "
					+ "a.href = url; " + "a.download = $1; " + "document.body.appendChild(a); " + "a.click(); "
					+ "window.URL.revokeObjectURL(url); " + "document.body.removeChild(a);", base64Pdf, fileName 
					// Pasamos el String Base64, NO el byte[]
			);
		} catch (Exception ex) {
			ex.printStackTrace();
			Notification.show("Error al generar el reporte: " + ex.getMessage());
		}
	}

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Enero";
            case 2 -> "Febrero";
            case 3 -> "Marzo";
            case 4 -> "Abril";
            case 5 -> "Mayo";
            case 6 -> "Junio";
            case 7 -> "Julio";
            case 8 -> "Agosto";
            case 9 -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "";
        };
    }
}
