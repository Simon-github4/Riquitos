package com.riquitos.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api/migracion")
@PermitAll
public class MigrationController {

    @Autowired
    private RecetaMigrationService migrationService;
    
    @GetMapping("/recalcular-recetas")
    public String recalcularRecetas() {
        long startTime = System.currentTimeMillis();
        
        int actualizados = migrationService.recalcularTodasLasRecetas();
        
        long duration = System.currentTimeMillis() - startTime;
        
        return String.format(
            "Migración completada!\n\n" +
            "Batches actualizados: %d\n" +
            "Tiempo de ejecución: %d ms\n\n" +
            "Nota: Eliminar esta URL después de usar.",
            actualizados, duration
        );
    }
}
