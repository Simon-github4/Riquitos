package com.riquitos.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.riquitos.backup.BackupService.BackupInfo;
import com.riquitos.base.ui.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.security.PermitAll;

@Route(value = "backups", layout = MainLayout.class)
@Menu(title = "Backups", order = 99, icon = "vaadin:database")
@PermitAll
//@Push 
public class BackupView extends VerticalLayout {

    private final BackupService backupService;
    private final Grid<BackupInfo> grid;
    private final Button createBackupBtn;
    private final ProgressBar progressBar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BackupView(BackupService backupService) {
        this.backupService = backupService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span title = new Span("Gestión de Backups");
        title.getStyle().set("font-size", "24px").set("font-weight", "bold");

        createBackupBtn = new Button("Crear Backup", new Icon(VaadinIcon.DATABASE), e -> createBackup());
        createBackupBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        header.add(title, createBackupBtn);

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        grid = new Grid<>(BackupInfo.class);
        grid.setSizeFull();
        grid.setColumns("name", "formattedSize");
        grid.getColumnByKey("name").setHeader("Nombre");
        grid.getColumnByKey("formattedSize").setHeader("Tamaño");
        
        grid.addComponentColumn(backup -> {
            long timestamp = backup.getTimestamp();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Span(sdf.format(new java.util.Date(timestamp)));
        }).setHeader("Fecha de creación").setSortable(true);

        grid.addComponentColumn(backup -> {
        	Anchor downloadAnchor = new Anchor(new StreamResource(backup.getName(), () -> {
                try {
                    return new FileInputStream(new File(backup.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }), "");
            
            downloadAnchor.getElement().setAttribute("download", true);
            Button downloadBtn = new Button(new Icon(VaadinIcon.DOWNLOAD));
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            downloadAnchor.add(downloadBtn);
            
            Button restoreBtn = new Button(new Icon(VaadinIcon.RECYCLE), e -> confirmarRestauracion(backup));
            restoreBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            restoreBtn.getElement().setProperty("title", "Restaurar");
            
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> deleteBackup(backup));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            
            return new HorizontalLayout(downloadAnchor, restoreBtn, deleteBtn);
        }).setHeader("Acciones");

        add(header, progressBar, grid);
        setFlexGrow(1, grid);

        refreshGrid();
    }

    private void refreshGrid() {
        try {
            List<BackupInfo> backups = backupService.listBackups();
            grid.setItems(backups);
        } catch (Exception e) {
            showNotification("Error al listar backups: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void createBackup() {
        createBackupBtn.setEnabled(false);
        progressBar.setVisible(true);
        
        UI ui = UI.getCurrent();
        // 2. Activamos el "Polling": El navegador preguntará cada 500ms si hay novedades
        ui.setPollInterval(500); 

        executor.execute(() -> {
            try {
                String backupPath = backupService.createBackup();
                String fileName = backupPath.substring(backupPath.lastIndexOf(File.separator) + 1);
                
                ui.access(() -> {
                    showNotification("Backup creado exitosamente: " + fileName, NotificationVariant.LUMO_SUCCESS);
                });
            } catch (Exception e) {
                ui.access(() -> {
                    showNotification("Error al crear backup: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                });
                e.printStackTrace();
            } finally {
                ui.access(() -> {
                    createBackupBtn.setEnabled(true);
                    progressBar.setVisible(false);
                    refreshGrid();
                    
                    // 3. ¡Importante! Desactivamos el Polling para no gastar recursos
                    ui.setPollInterval(-1); 
                });
            }
        });
    }

    private void ejecutarRestauracion(BackupInfo backup) {
        progressBar.setVisible(true);
        grid.setEnabled(false); 

        UI ui = UI.getCurrent();
        ui.setPollInterval(500); 

        executor.execute(() -> {
            try {
                boolean success = backupService.restoreBackup(backup.getPath());

                ui.access(() -> {
                    if (success) 
                        showNotification("Base de datos restaurada exitosamente", NotificationVariant.LUMO_SUCCESS);
                    else 
                        showNotification("La restauración no reportó éxito", NotificationVariant.LUMO_CONTRAST);
                });
            } catch (Exception e) {
                ui.access(() -> {
                    showNotification("Error al restaurar: " + e.getMessage(), NotificationVariant.LUMO_ERROR);});
                e.printStackTrace();
            } finally {
                ui.access(() -> {
                    progressBar.setVisible(false);
                    grid.setEnabled(true);
                    ui.setPollInterval(-1); 
                });
            }
        });
    }

    private void deleteBackup(BackupInfo backup) {
        try {
            backupService.deleteBackup(backup.getPath());
            showNotification("Backup eliminado", NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        } catch (Exception e) {
            showNotification("Error al eliminar: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmarRestauracion(BackupInfo backup) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚠️ Confirmar Restauración");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        
        dialogLayout.add(new Paragraph("Estás a punto de restaurar la base de datos al estado del archivo:"));
        Paragraph fileName = new Paragraph(backup.getName());
        fileName.getStyle().set("font-weight", "bold");
        dialogLayout.add(fileName);
        
        Paragraph warning = new Paragraph("¡CUIDADO! Todos los datos actuales que no estén en este backup se perderán permanentemente. (asegurate de tener un backup reciente antes de continuar)");
        warning.getStyle().set("color", "var(--lumo-error-text-color)");
        dialogLayout.add(warning);

        dialog.add(dialogLayout);

        // Botón Cancelar
        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        
        // Botón Confirmar (Rojo para indicar peligro)
        Button confirmButton = new Button("Sí, Restaurar", e -> {
            dialog.close();
            ejecutarRestauracion(backup); // <--- Llamamos al método real aquí
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.setIcon(new Icon(VaadinIcon.WARNING));

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(confirmButton);

        dialog.open();
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        getUI().ifPresent(ui -> ui.access(() -> {
            Notification notification = new Notification(message, 5000, Position.MIDDLE);
            notification.addThemeVariants(variant);
            notification.open();
        }));
    }
}
