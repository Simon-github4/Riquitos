package com.riquitos.reports;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

import com.riquitos.base.ui.MainLayout;
import com.riquitos.base.ui.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@PageTitle("Reportes")
@Route(value = "reports", layout = MainLayout.class)
@Menu(order = 10, icon = "vaadin:chart", title = "Reportes")
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class ReportsView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService;

    private ComboBox<String> reportTypeCombo;
    private ComboBox<Integer> yearCombo;
    private ComboBox<Integer> monthCombo;
    private DatePicker weekDatePicker;
    private Span weekRangeLabel; // Nueva etiqueta para mostrar el rango
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
        filtersLayout.setWrap(true);
        filtersLayout.setAlignItems(Alignment.END);

        reportTypeCombo = new ComboBox<>("Tipo de Reporte");
        reportTypeCombo.setItems("Semanal", "Mensual", "Anual");
        reportTypeCombo.setValue("Mensual");
        reportTypeCombo.setWidth("200px");
        reportTypeCombo.addValueChangeListener(e -> updateFieldsVisibility());

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

        // Componente para la fecha de la semana
        weekDatePicker = new DatePicker("Seleccione una fecha (semana)");
        weekDatePicker.setValue(LocalDate.now());
        weekDatePicker.setWidth("200px");
        weekDatePicker.setVisible(false);
        
        // Etiqueta visual para mostrar el rango de fechas al lado
        weekRangeLabel = new Span();
        weekRangeLabel.setVisible(false);
        // Le damos un poco de estilo para que se vea como texto secundario y se alinee bien
        weekRangeLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        weekRangeLabel.getStyle().set("padding-bottom", "10px"); 
        weekRangeLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        // Listener para actualizar el rango cada vez que cambia la fecha
        weekDatePicker.addValueChangeListener(e -> updateWeekRangeText(e.getValue()));
        
        // Inicializamos el texto con la fecha actual
        updateWeekRangeText(weekDatePicker.getValue());

        filtersLayout.add(reportTypeCombo, weekDatePicker, weekRangeLabel, yearCombo, monthCombo);
        add(filtersLayout);
    }

    private void createGenerateButton() {
        generatePdfButton = new Button("Generar PDF");
        generatePdfButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generatePdfButton.addClickListener(e -> generatePdf());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(generatePdfButton);
        buttonLayout.setPadding(true);
        add(buttonLayout);
    }

    // Nuevo método para calcular y actualizar el texto de la semana
    private void updateWeekRangeText(LocalDate date) {
        if (date == null) {
            weekRangeLabel.setText("");
            return;
        }
        LocalDate start = date.with(DayOfWeek.MONDAY);
        LocalDate end = date.with(DayOfWeek.SUNDAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        weekRangeLabel.setText("(Se reportará del " + start.format(formatter) + " al " + end.format(formatter) + ")");
    }

    private void updateFieldsVisibility() {
        String type = reportTypeCombo.getValue();
        
        boolean isWeekly = "Semanal".equals(type);
        boolean isMonthly = "Mensual".equals(type);
        boolean isYearly = "Anual".equals(type);

        weekDatePicker.setVisible(isWeekly);
        weekRangeLabel.setVisible(isWeekly); // Controlamos también la visibilidad de la etiqueta
        
        monthCombo.setVisible(isMonthly);
        yearCombo.setVisible(isMonthly || isYearly);
    }

    private void generatePdf() {
        try {
            String reportType = reportTypeCombo.getValue();
            if (reportType == null) return;

            byte[] pdfBytes;
            String fileName;

            if ("Semanal".equals(reportType)) {
                LocalDate selectedDate = weekDatePicker.getValue();
                if (selectedDate == null) {
                    Notification.show("Debe seleccionar una fecha.");
                    return;
                }
                pdfBytes = reportService.generateWeeklyReport(selectedDate);
                
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd_MM_yyyy");
                fileName = "reporte_semanal_" + selectedDate.format(fmt) + ".pdf";

            } else {
                Integer year = yearCombo.getValue();
                if (year == null) return;

                if ("Mensual".equals(reportType)) {
                    Integer month = monthCombo.getValue();
                    if (month == null) return;
                    
                    pdfBytes = reportService.generateMonthlyReport(year, month);
                    fileName = "reporte_mensual_" + getMonthName(month) + "_" + year + ".pdf";
                } else {
                    pdfBytes = reportService.generateYearlyReport(year);
                    fileName = "reporte_anual_" + year + ".pdf";
                }
            }

            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

            UI.getCurrent().getPage().executeJs("const byteCharacters = atob($0); " +
                    "const byteNumbers = new Array(byteCharacters.length); "
                    + "for (let i = 0; i < byteCharacters.length; i++) { "
                    + "    byteNumbers[i] = byteCharacters.charCodeAt(i); " + "} "
                    + "const byteArray = new Uint8Array(byteNumbers); " +
                    "const blob = new Blob([byteArray], { type: 'application/pdf' }); " +
                    "const url = window.URL.createObjectURL(blob); " + "const a = document.createElement('a'); "
                    + "a.href = url; " + "a.download = $1; " + "document.body.appendChild(a); " + "a.click(); "
                    + "window.URL.revokeObjectURL(url); " + "document.body.removeChild(a);", base64Pdf, fileName
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