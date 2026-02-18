package com.riquitos.reports;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.riquitos.production.ProductionBatch;
import com.riquitos.production.ProductionBatchService;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;
import com.riquitos.stock.StockMovement;
import com.riquitos.stock.StockMovementService;

@Service
public class ReportService {

    private final ProductionBatchService productionBatchService;
    private final StockMovementService stockMovementService;
    private final RawMaterialService rawMaterialService;

    public ReportService(ProductionBatchService productionBatchService,
    					StockMovementService stockMovementService,
    					RawMaterialService rawMaterialService) {
        this.productionBatchService = productionBatchService;
        this.stockMovementService = stockMovementService;
        this.rawMaterialService = rawMaterialService;
    }

    public byte[] generateMonthlyReport(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return fetchAndGenerate(startDate, endDate, "Reporte Mensual - " + getMonthName(month) + " " + year);
    }

    public byte[] generateYearlyReport(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return fetchAndGenerate(startDate, endDate, "Reporte Anual - " + year);
    }

    // Método helper para evitar duplicar la lógica de búsqueda
    private byte[] fetchAndGenerate(LocalDateTime startDate, LocalDateTime endDate, String title) {
        List<ProductionBatch> productions = productionBatchService.findAll().stream()
                .filter(p -> p.getProductionDate() != null 
                        && !p.getProductionDate().isBefore(startDate) 
                        && !p.getProductionDate().isAfter(endDate))
                .toList();

        List<StockMovement> movements = stockMovementService.findAll().stream()
                .filter(m -> m.getMovementDateTime() != null 
                        && !m.getMovementDateTime().isBefore(startDate) 
                        && !m.getMovementDateTime().isAfter(endDate))
                .toList();

        return createPdf(title, productions, movements);
    }

    private byte[] createPdf(String reportTitle, List<ProductionBatch> productions, List<StockMovement> movements) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // --- TÍTULO ---
            document.add(new Paragraph(reportTitle)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n"));

            // ==========================================
            // 1. RESUMEN DE PRODUCCIÓN POR PRODUCTO
            // ==========================================
            document.add(new Paragraph("RESUMEN DE PRODUCCIÓN (Por Producto)")
                    .setFontSize(14)
                    .setBold());

            // Lógica de Agregación para Producción
            // Mapa: NombreProducto -> Estadísticas
            Map<String, ProductionStats> prodStatsMap = new TreeMap<>(); // TreeMap para orden alfabético

            for (ProductionBatch p : productions) {
                String prodName = p.getProduct() != null ? p.getProduct().getDescription() : "Desconocido";
                
                ProductionStats stats = prodStatsMap.computeIfAbsent(prodName, k -> new ProductionStats());
                
                stats.count++; // Cantidad de lotes
                if (p.getUnitiesProduced() != null) {
                    stats.totalUnits = stats.totalUnits.add(p.getUnitiesProduced());
                }
                if (p.getBagsOrBoxProduced() != null) {
                    stats.totalBags = stats.totalBags.add(p.getBagsOrBoxProduced());
                }
            }

            // Tabla de Producción
            if (!prodStatsMap.isEmpty()) {
                Table prodTable = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
                prodTable.setWidth(UnitValue.createPercentValue(100));
                
                prodTable.addHeaderCell(createHeaderCell("Producto"));
                prodTable.addHeaderCell(createHeaderCell("Cant. Lotes"));
                prodTable.addHeaderCell(createHeaderCell("Total Unidades"));
                prodTable.addHeaderCell(createHeaderCell("Total Bolsones/Cajas"));

                // Totales generales para el pie de tabla
                int grandTotalLotes = 0;
                BigDecimal grandTotalUnits = BigDecimal.ZERO;
                BigDecimal grandTotalBags = BigDecimal.ZERO;

                for (Map.Entry<String, ProductionStats> entry : prodStatsMap.entrySet()) {
                    ProductionStats stats = entry.getValue();
                    prodTable.addCell(createCell(entry.getKey()));
                    prodTable.addCell(createCell(String.valueOf(stats.count)));
                    prodTable.addCell(createCell(stats.totalUnits.toString()));
                    prodTable.addCell(createCell(stats.totalBags.toString()));

                    grandTotalLotes += stats.count;
                    grandTotalUnits = grandTotalUnits.add(stats.totalUnits);
                    grandTotalBags = grandTotalBags.add(stats.totalBags);
                }

                // Fila de Totales Generales
                prodTable.addCell(createHeaderCell("TOTALES"));
                prodTable.addCell(createHeaderCell(String.valueOf(grandTotalLotes)));
                prodTable.addCell(createHeaderCell(grandTotalUnits.toString()));
                prodTable.addCell(createHeaderCell(grandTotalBags.toString()));

                document.add(prodTable);
            } else {
                document.add(new Paragraph("No hubo producción en este periodo."));
            }

            document.add(new Paragraph("\n"));

            // ==========================================
            // 2. RESUMEN DE MOVIMIENTOS DE INSUMOS
            // ==========================================
            document.add(new Paragraph("RESUMEN DE MOVIMIENTOS DE STOCK(Ingresos y Egresos)")
                    .setFontSize(14)
                    .setBold());

            // Lógica de Agregación para Movimientos
            // Mapa: NombreMateriaPrima -> Estadísticas
            Map<String, MovementStats> moveStatsMap = new TreeMap<>();

            for (StockMovement m : movements) {
                String materialName = m.getRawMaterial() != null ? m.getRawMaterial().getName()+" ("+m.getRawMaterial().getUnit()+")" : "Desconocido";
                BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ZERO;

                MovementStats stats = moveStatsMap.computeIfAbsent(materialName, k -> new MovementStats());

                if (m.getType() == StockMovement.StockMovementType.INGRESO) {
                    stats.totalIngresos = stats.totalIngresos.add(qty);
                } else if (m.getType() == StockMovement.StockMovementType.EGRESO) {
                    stats.totalEgresos = stats.totalEgresos.add(qty);
                }
            }

            if (!moveStatsMap.isEmpty()) {
                Table moveTable = new Table(UnitValue.createPercentArray(new float[]{4, 3, 3, 3}));
                moveTable.setWidth(UnitValue.createPercentValue(100));
                
                moveTable.addHeaderCell(createHeaderCell("Materia Prima"));
                moveTable.addHeaderCell(createHeaderCell("Total Ingresos"));
                moveTable.addHeaderCell(createHeaderCell("Total Egresos"));
                moveTable.addHeaderCell(createHeaderCell("Balance (Ing - Egr)"));

                for (Map.Entry<String, MovementStats> entry : moveStatsMap.entrySet()) {
                    MovementStats stats = entry.getValue();
                    BigDecimal balance = stats.totalIngresos.subtract(stats.totalEgresos);

                    moveTable.addCell(createCell(entry.getKey()));
                    moveTable.addCell(createCell(stats.totalIngresos.toString()));
                    moveTable.addCell(createCell(stats.totalEgresos.toString()));
                    // Color condicional para el balance si es negativo (opcional)
                    moveTable.addCell(createCell(balance.toString()));
                }
                document.add(moveTable);
            } else {
                document.add(new Paragraph("No hubo movimientos de stock en este periodo."));
            }

            document.add(new Paragraph("\n"));

            // ==========================================
            // 3. STOCK ACTUAL (SNAPSHOT)
            // ==========================================
            document.add(new Paragraph("ESTADO ACTUAL DE INVENTARIO ("+LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()+")")
                    .setFontSize(14)
                    .setBold());
            
            List<RawMaterial> rawMaterials = rawMaterialService.findAll();
            if (!rawMaterials.isEmpty()) {
                Table stockTable = new Table(UnitValue.createPercentArray(new float[]{4, 2, 3}));
                stockTable.setWidth(UnitValue.createPercentValue(100));
                
                stockTable.addHeaderCell(createHeaderCell("Materia Prima"));
                stockTable.addHeaderCell(createHeaderCell("Unidad"));
                stockTable.addHeaderCell(createHeaderCell("Stock Disponible Hoy"));

                for (RawMaterial rm : rawMaterials) {
                    stockTable.addCell(createCell(rm.getName() != null ? rm.getName() : ""));
                    stockTable.addCell(createCell(rm.getUnit() != null ? rm.getUnit() : ""));
                    stockTable.addCell(createCell(rm.getCurrentStock() != null 
                            ? rm.getCurrentStock().toString() : "0"));
                }
                
                document.add(stockTable);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }

        return baos.toByteArray();
    }

    // --- Clases Helper para Agregación (Internas) ---
    
    private static class ProductionStats {
        int count = 0;
        BigDecimal totalUnits = BigDecimal.ZERO;
        BigDecimal totalBags = BigDecimal.ZERO;
    }

    private static class MovementStats {
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
    }

    // --- Helpers de Estilo ---

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text != null ? text : ""))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Enero"; case 2 -> "Febrero"; case 3 -> "Marzo";
            case 4 -> "Abril"; case 5 -> "Mayo"; case 6 -> "Junio";
            case 7 -> "Julio"; case 8 -> "Agosto"; case 9 -> "Septiembre";
            case 10 -> "Octubre"; case 11 -> "Noviembre"; case 12 -> "Diciembre";
            default -> "";
        };
    }
}