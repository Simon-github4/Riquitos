package com.riquitos.reports;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
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

    // ==========================================
    // MÉTODOS PÚBLICOS DE GENERACIÓN
    // ==========================================

    public byte[] generateWeeklyReport(LocalDate date) {
        LocalDateTime startDate = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endDate = date.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String title = "Reporte Semanal (" + startDate.format(formatter) + " al " + endDate.format(formatter) + ")";
        
        return fetchAndGenerate(startDate, endDate, title);
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

    // ==========================================
    // LÓGICA PRINCIPAL (PDF)
    // ==========================================

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
            // 1. RESUMEN DE PRODUCCIÓN POR CATEGORÍA
            // ==========================================
			document.add(new Paragraph("RESUMEN DE PRODUCCIÓN (Por Categoría)").setFontSize(14).setBold());

			Map<String, ProductionStats> catStatsMap = new TreeMap<>();

			for (ProductionBatch p : productions) {
				String catName = (p.getProduct() != null && p.getProduct().getCategory() != null)
						? p.getProduct().getCategory().getName()
						: "Sin Categoría";

				ProductionStats stats = catStatsMap.computeIfAbsent(catName, k -> new ProductionStats());

				stats.count++;
				if (p.getUnitiesProduced() != null) {
					BigDecimal units = p.getUnitiesProduced();
					stats.totalUnits = stats.totalUnits.add(units);

					// Cálculo de KG: (Unidades * peso_gramos) / 1000
					if (p.getProduct() != null) {
						BigDecimal weightGrams = BigDecimal.valueOf(p.getProduct().getNetWeight());
						BigDecimal batchKg = units.multiply(weightGrams).divide(new BigDecimal("1000"));
						stats.totalKg = stats.totalKg.add(batchKg);
					}
				}
				if (p.getBagsOrBoxProduced() != null) {
					stats.totalBags = stats.totalBags.add(p.getBagsOrBoxProduced());
				}
			}

			if (!catStatsMap.isEmpty()) {
				// Tabla con 5 columnas ahora (Categoría, Lotes, Unidades, Bolsas, KG)
				Table catTable = new Table(UnitValue.createPercentArray(new float[] { 3, 2, 2, 2, 2 }));
				catTable.setWidth(UnitValue.createPercentValue(100));

				catTable.addHeaderCell(createHeaderCell("Categoría"));
				catTable.addHeaderCell(createHeaderCell("Cant. Lotes"));
				catTable.addHeaderCell(createHeaderCell("Total Unidades"));
				catTable.addHeaderCell(createHeaderCell("Total Bolsones"));
				catTable.addHeaderCell(createHeaderCell("Total KG"));

				int grandTotalLotes = 0;
				BigDecimal grandTotalUnits = BigDecimal.ZERO;
				BigDecimal grandTotalBags = BigDecimal.ZERO;
				BigDecimal grandTotalKg = BigDecimal.ZERO;

				for (Map.Entry<String, ProductionStats> entry : catStatsMap.entrySet()) {
					ProductionStats stats = entry.getValue();
					catTable.addCell(createCell(entry.getKey()));
					catTable.addCell(createCell(String.valueOf(stats.count)));
					catTable.addCell(createCell(stats.totalUnits.toString()));
					catTable.addCell(createCell(stats.totalBags.toString()));
					catTable.addCell(createCell(String.format("%.2f kg", stats.totalKg))); // Formateo a 2 decimales

					grandTotalLotes += stats.count;
					grandTotalUnits = grandTotalUnits.add(stats.totalUnits);
					grandTotalBags = grandTotalBags.add(stats.totalBags);
					grandTotalKg = grandTotalKg.add(stats.totalKg);
				}

				// Fila de Totales Generales
				catTable.addCell(createHeaderCell("TOTALES"));
				catTable.addCell(createHeaderCell(String.valueOf(grandTotalLotes)));
				catTable.addCell(createHeaderCell(grandTotalUnits.toString()));
				catTable.addCell(createHeaderCell(grandTotalBags.toString()));
				catTable.addCell(createHeaderCell(String.format("%.2f kg", grandTotalKg)));

				document.add(catTable);
			} else 
				document.add(new Paragraph("No hubo producción en este periodo."));

			document.add(new Paragraph("\n"));

            // ==========================================
            // 2. RESUMEN DE MOVIMIENTOS DE INSUMOS
            // ==========================================
            document.add(new Paragraph("RESUMEN DE MOVIMIENTOS DE STOCK (Ingresos, Egresos y Ajustes)")
                    .setFontSize(14)
                    .setBold());

            Map<String, MovementStats> moveStatsMap = new TreeMap<>();

            for (StockMovement m : movements) {
                String materialName = m.getRawMaterial() != null ? m.getRawMaterial().getName()+" ("+m.getRawMaterial().getUnit()+")" : "Desconocido";
                BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ZERO;

                MovementStats stats = moveStatsMap.computeIfAbsent(materialName, k -> new MovementStats());

                if (m.getType() == StockMovement.StockMovementType.INGRESO) {
                    stats.totalIngresos = stats.totalIngresos.add(qty);
                } else if (m.getType() == StockMovement.StockMovementType.EGRESO) {
                    stats.totalEgresos = stats.totalEgresos.add(qty);
                } else if (m.getType() == StockMovement.StockMovementType.AJUSTE) { 
                    stats.totalAjustes = stats.totalAjustes.add(qty);
                }
            }

            if (!moveStatsMap.isEmpty()) {
                // 5 columnas ahora para incluir los Ajustes
                Table moveTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}));
                moveTable.setWidth(UnitValue.createPercentValue(100));
                
                moveTable.addHeaderCell(createHeaderCell("Materia Prima"));
                moveTable.addHeaderCell(createHeaderCell("Ingresos"));
                moveTable.addHeaderCell(createHeaderCell("Egresos"));
                moveTable.addHeaderCell(createHeaderCell("Ajustes"));
                moveTable.addHeaderCell(createHeaderCell("Balance (Ing - Egr + Aju)"));

                for (Map.Entry<String, MovementStats> entry : moveStatsMap.entrySet()) {
                    MovementStats stats = entry.getValue();
                    BigDecimal balance = stats.totalIngresos.subtract(stats.totalEgresos).add(stats.totalAjustes);

                    moveTable.addCell(createCell(entry.getKey()));
                    moveTable.addCell(createCell(stats.totalIngresos.toString()));
                    moveTable.addCell(createCell(stats.totalEgresos.toString()));
                    moveTable.addCell(createCell(stats.totalAjustes.toString()));
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

    // ==========================================
    // CLASES HELPER (INTERNAS)
    // ==========================================
    
    private static class ProductionStats {
        int count = 0;
        BigDecimal totalUnits = BigDecimal.ZERO;
        BigDecimal totalBags = BigDecimal.ZERO;
        BigDecimal totalKg = BigDecimal.ZERO; 
    }

    private static class MovementStats {
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        BigDecimal totalAjustes = BigDecimal.ZERO; // <-- Nuevo campo
    }

    // ==========================================
    // HELPERS DE ESTILO Y FORMATEO
    // ==========================================

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