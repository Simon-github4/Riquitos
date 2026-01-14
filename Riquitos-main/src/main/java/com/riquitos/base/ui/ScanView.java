package com.riquitos.base.ui;

import java.math.BigDecimal;
import java.util.List;

import com.riquitos.entities.Product; // Asegúrate de importar tu entidad
import com.riquitos.product.ProductService; // Importar el servicio

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PageTitle("QR Scanner")
@Route(value = "scan-qr", layout = MainLayout.class)
@NpmPackage(value = "html5-qrcode", version = "2.3.8")
@Menu(order = 3, icon = "vaadin:clipboard-check", title = "QR")
@PermitAll
public class ScanView extends VerticalLayout {

    // SERVICIOS
    private final ProductService productService; // NUEVO: Servicio inyectado

    // UI COMPONENTS
    private final TextField codigoQrField; 
    private final TextField productoSeleccionadoField; // NUEVO: Para mostrar el producto real elegido
    private final BigDecimalField cantidadField;
    private final Div cameraContainer;
    private final Button toggleCameraBtn;
    
    // ESTADO
    private boolean isScanning = false;
    private Product selectedProduct; // NUEVO: Guardamos el objeto producto, no solo el string

    // Constructor con inyección de dependencia
    public ScanView(ProductService productService) { 
        this.productService = productService; // NUEVO: Asignación

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new ViewToolbar("Registro de Lote"));

        // CONTENEDOR CÁMARA
        cameraContainer = new Div();
        cameraContainer.setId("reader");
        cameraContainer.setWidthFull();
        cameraContainer.setMaxWidth("300px");
        cameraContainer.setHeight("300px");
        
        cameraContainer.getStyle().set("border", "1px solid #e0e0e0");
        cameraContainer.getStyle().set("border-radius", "8px");
        cameraContainer.getStyle().set("overflow", "hidden");
        cameraContainer.getStyle().set("background-color", "#f5f5f5"); 

        // BOTÓN CÁMARA
        toggleCameraBtn = new Button("Iniciar Escáner");
        toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleCameraBtn.setEnabled(false);
        toggleCameraBtn.setWidthFull();
        toggleCameraBtn.setMaxWidth("300px");
        toggleCameraBtn.addClickListener(e -> toggleCamera());

        // CAMPOS DE DATOS
        codigoQrField = new TextField("Texto Escaneado (Búsqueda)");
        codigoQrField.setReadOnly(true);
        codigoQrField.setWidthFull();
        codigoQrField.setMaxWidth("300px");
        codigoQrField.setPlaceholder("Esperando escaneo...");

        // NUEVO: Campo para mostrar qué producto se eligió finalmente
        productoSeleccionadoField = new TextField("Producto Identificado");
        productoSeleccionadoField.setReadOnly(true);
        productoSeleccionadoField.setWidthFull();
        productoSeleccionadoField.setMaxWidth("300px");
        productoSeleccionadoField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_HELPER_ABOVE_FIELD);

        cantidadField = new BigDecimalField("Cantidad / Peso");
        cantidadField.setWidthFull();
        cantidadField.setMaxWidth("300px");
        cantidadField.setPlaceholder("0.00");

        Button confirmarBtn = new Button("Confirmar y Guardar");
        confirmarBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        confirmarBtn.setWidthFull();
        confirmarBtn.setMaxWidth("300px");
        confirmarBtn.addClickListener(e -> confirmar());

        // Añadimos el nuevo campo al layout
        add(cameraContainer, toggleCameraBtn, codigoQrField, productoSeleccionadoField, cantidadField, confirmarBtn);

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
    }

    @ClientCallable
    public void onScanSuccess(String decodedText) {
        getUI().ifPresent(ui -> ui.access(() -> {
            
        	if (!isScanning) 
                return;

        	isScanning = false;

        	stopCameraJS();
            toggleCameraBtn.setText("Iniciar Escáner");
            toggleCameraBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleCameraBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            codigoQrField.setValue(decodedText);
            Notification.show("Código leído: " + decodedText, 1000, Notification.Position.MIDDLE);

            // 2. NUEVO: Lógica de búsqueda en base de datos
            buscarYSeleccionarProducto(decodedText);
        }));
    }

    // NUEVO: Método central de lógica de negocio
    private void buscarYSeleccionarProducto(String textoBusqueda) {
        // Limpiar selección previa
        this.selectedProduct = null;
        this.productoSeleccionadoField.clear();

        List<Product> resultados = productService.findAll(textoBusqueda);

        if (resultados.isEmpty()) {
            Notification.show("No se encontraron productos con esa descripción.", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cantidadField.setEnabled(false);
            
        } else if (resultados.size() == 1) {
            // Caso ideal: Solo hay uno, lo seleccionamos directo
            seleccionarProducto(resultados.get(0));
            
        } else {
            // Caso múltiple: Abrir diálogo para elegir
            abrirDialogoSeleccion(resultados);
        }
    }

    // NUEVO: Diálogo para desambiguar
    private void abrirDialogoSeleccion(List<Product> productos) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleccione un producto");
        
        Grid<Product> grid = new Grid<>(Product.class, false);
        // Ajusta estas columnas según los atributos reales de tu entidad Product
        grid.addColumn(Product::toString).setHeader("Descripción"); 
        // grid.addColumn(Product::getCodigo).setHeader("Código"); 
        
        grid.setItems(productos);
        grid.setWidth("100%");
        
        grid.addItemClickListener(event -> {
            seleccionarProducto(event.getItem());
            dialog.close();
        });

        VerticalLayout layout = new VerticalLayout(grid);
        layout.setPadding(false);
        dialog.add(layout);
        
        Button cancelar = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancelar);
        
        dialog.open();
    }

    // NUEVO: Establece el producto en el estado de la vista
    private void seleccionarProducto(Product p) {
        this.selectedProduct = p;
        // Asumiendo que Product tiene un método toString() o getDescription() útil
        this.productoSeleccionadoField.setValue(p.toString()); 
        this.cantidadField.setEnabled(true);
        this.cantidadField.focus();
        
        Notification.show("Producto seleccionado.", 2000, Notification.Position.BOTTOM_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
            
            // Limpiar campos al iniciar nuevo escaneo
            codigoQrField.clear();
            productoSeleccionadoField.clear();
            cantidadField.clear();
            selectedProduct = null;
        }
        isScanning = !isScanning;
    }

    private void confirmar() {
        if (selectedProduct == null) {
            Notification.show("Debe escanear y seleccionar un producto válido", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (cantidadField.isEmpty()) {
            Notification.show("Ingrese una cantidad", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // --- AQUÍ TU LÓGICA DE GUARDADO EN BASE DE DATOS ---
        // Ahora usas this.selectedProduct (Objeto real) y cantidadField.getValue()
        
        BigDecimal cantidad = cantidadField.getValue();
        
        // Ejemplo: productionService.save(selectedProduct, cantidad);
        
        Notification.show("Producción registrada: " + selectedProduct.toString() + " x " + cantidad, 
                3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Limpieza para el siguiente
        codigoQrField.clear();
        productoSeleccionadoField.clear();
        cantidadField.clear();
        selectedProduct = null;
        cantidadField.setInvalid(false);
    }

    private void startCameraJS() {
        String js = 
            "if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {" +
            "    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })" +
            "    .then(function(stream) {" +
            "        stream.getTracks().forEach(track => track.stop());" + 
            "        " +
            "        if (!window.Html5Qrcode) return;" +
            "        const html5QrCode = new Html5Qrcode('reader');" +
            "        window.currentQrScanner = html5QrCode;" +
            "        " +
            "        html5QrCode.start(" +
            "          { facingMode: 'environment' }," + 
            "          { fps: 10, qrbox: { width: 250, height: 250 } }," +
            "          (decodedText, decodedResult) => {" +
            "             /* --- MODIFICACIÓN: SONIDO Y PAUSA --- */" +
            "             try {" +
            "                 /* 1. Generar Bip electrónico corto */" +
            "                 var ctx = new (window.AudioContext || window.webkitAudioContext)();" +
            "                 var osc = ctx.createOscillator();" +
            "                 osc.connect(ctx.destination);" +
            "                 osc.frequency.value = 600;" + // Tono agudo
            "                 osc.start();" +
            "                 osc.stop(ctx.currentTime + 0.1);" + // Dura 0.1 segundos
            "             } catch(e) { console.log('Audio error (ignorable)', e); }" +
            "             " +
            "             try {" +
            "                 /* 2. Pausar cámara para evitar lectura doble visual */" +
            "                 html5QrCode.pause();" +
            "             } catch(e) {}" +
            "             /* ------------------------------------- */" +
            "             " +
            "             $0.$server.onScanSuccess(decodedText);" +
            "          }," +
            "          (errorMessage) => { }" + 
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