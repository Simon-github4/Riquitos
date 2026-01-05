package com.riquitos.base.ui;

import java.math.BigDecimal;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import com.riquitos.base.ui.MainLayout;

@PageTitle("QR Scanner")
@Route(value = "scan-qr", layout = MainLayout.class)
@NpmPackage(value = "html5-qrcode", version = "2.3.8") // Descarga automática de la librería JS
@Menu(order = 3, icon = "vaadin:clipboard-check", title = "QR")
@PermitAll
public class ScanView extends VerticalLayout {

    private final TextField codigoQrField;
    private final BigDecimalField cantidadField;
    private final Div cameraContainer;
    private final Button toggleCameraBtn;
    private boolean isScanning = false;

    public ScanView() {
    	setWidthFull();
        setAlignItems(Alignment.CENTER); // Centrar horizontalmente
        setJustifyContentMode(JustifyContentMode.CENTER); // Centrar verticalmente (si entra)

        add(new ViewToolbar("Registro de Lote"));
        //add(new H2("Registro de Lote"));

        // 2. CONTENEDOR CÁMARA
        cameraContainer = new Div();
        cameraContainer.setId("reader");
        cameraContainer.setWidthFull();
        cameraContainer.setMaxWidth("300px");
        cameraContainer.setHeight("300px"); // El alto fijo está bien para la cámara
        
        cameraContainer.getStyle().set("border", "1px solid #e0e0e0");
        cameraContainer.getStyle().set("border-radius", "8px");
        cameraContainer.getStyle().set("overflow", "hidden"); // Esto es interno de la cámara, está bien
        cameraContainer.getStyle().set("background-color", "#f5f5f5"); 

        // 3. BOTÓN CÁMARA
        toggleCameraBtn = new Button("Iniciar Escáner");
        toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleCameraBtn.setEnabled(false);
        toggleCameraBtn.setWidthFull();      // Ancho adaptable
        toggleCameraBtn.setMaxWidth("300px"); // Tope máximo
        toggleCameraBtn.addClickListener(e -> toggleCamera());

        // 4. CAMPOS DE DATOS (Responsive)
        codigoQrField = new TextField("Código Lote");
        codigoQrField.setReadOnly(true);
        codigoQrField.setWidthFull();
        codigoQrField.setMaxWidth("300px");
        codigoQrField.setPlaceholder("Esperando escaneo...");

        cantidadField = new BigDecimalField("Cantidad / Peso");
        cantidadField.setWidthFull();
        cantidadField.setMaxWidth("300px");
        cantidadField.setPlaceholder("0.00");

        Button confirmarBtn = new Button("Confirmar y Guardar");
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        confirmarBtn.setWidthFull();
        confirmarBtn.setMaxWidth("300px");
        confirmarBtn.addClickListener(e -> confirmar());

        
        add(cameraContainer, toggleCameraBtn, codigoQrField, cantidadField, confirmarBtn);

        cargarLibreria();
    }

    private void cargarLibreria() {
        String initJs = 
            "var script = document.createElement('script');" +
            "script.src = 'https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js';" +
            "script.onload = function() {" +
            "   $0.$server.onLibraryLoaded();" +
            "};" +
            "document.head.appendChild(script);";
        
        UI.getCurrent().getPage().executeJs(initJs, getElement());
    }

    @ClientCallable
    public void onLibraryLoaded() {
        toggleCameraBtn.setEnabled(true);
        // Opcional: Auto-iniciar cámara si quieres ahorrar un clic al usuario
        // toggleCamera(); 
    }

    @ClientCallable
    public void onScanSuccess(String decodedText) {
        getUI().ifPresent(ui -> ui.access(() -> {
            codigoQrField.setValue(decodedText);
            
            // Feedback sonoro o visual sutil
            Notification.show("Código leído", 1000, Notification.Position.MIDDLE);
            
            // Detener cámara automáticamente tras éxito
            stopCameraJS();
            isScanning = false;
            toggleCameraBtn.setText("Iniciar Escáner");
            toggleCameraBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            // Foco al campo de cantidad para ingreso rápido
            cantidadField.focus();
        }));
    }

    private void toggleCamera() {
        if (isScanning) {
            stopCameraJS();
            toggleCameraBtn.setText("Iniciar Escáner");
            toggleCameraBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            startCameraJS();
            toggleCameraBtn.setText("Cancelar");
            toggleCameraBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        }
        isScanning = !isScanning;
    }

    private void confirmar() {
        if (codigoQrField.isEmpty() || cantidadField.isEmpty()) {
            Notification.show("Faltan datos por completar", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // --- AQUÍ TU LÓGICA DE GUARDADO EN BASE DE DATOS ---
        String lote = codigoQrField.getValue();
        BigDecimal cantidad = cantidadField.getValue();
        
        // Simulación de guardado
        Notification.show("Guardado: " + lote + " (" + cantidad + ")", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Limpieza para el siguiente
        codigoQrField.clear();
        cantidadField.clear();
        cantidadField.setInvalid(false);
    }

    private void startCameraJS() {
        // Lógica limpia: Solicita permisos nativos silenciosamente y arranca el escáner
        String js = 
            "if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {" +
            "    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })" +
            "    .then(function(stream) {" +
            "        stream.getTracks().forEach(track => track.stop());" + // Liberamos el test
            "        " +
            "        if (!window.Html5Qrcode) return;" +
            "        const html5QrCode = new Html5Qrcode('reader');" +
            "        window.currentQrScanner = html5QrCode;" +
            "        " +
            "        html5QrCode.start(" +
            "          { facingMode: 'environment' }," + 
            "          { fps: 10, qrbox: { width: 250, height: 250 } }," +
            "          (decodedText, decodedResult) => {" +
            "             $0.$server.onScanSuccess(decodedText);" +
            "          }," +
            "          (errorMessage) => { }" + // Errores de frame ignorados silenciosamente
            "        ).catch(err => console.error(err));" +
            "    })" +
            "    .catch(function(err) {" +
            "        console.error('Permiso denegado:', err);" +
            "        alert('Necesitamos acceso a la cámara para escanear.');" +
            "    });" +
            "}";
        
        UI.getCurrent().getPage().executeJs(js, getElement());
    }

    private void stopCameraJS() {
        String js = 
            "if (window.currentQrScanner) {" +
            "   window.currentQrScanner.stop().then(() => {" +
            "       window.currentQrScanner.clear();" +
            "   }).catch(err => console.error(err));" +
            "}";
        UI.getCurrent().getPage().executeJs(js);
    }
}