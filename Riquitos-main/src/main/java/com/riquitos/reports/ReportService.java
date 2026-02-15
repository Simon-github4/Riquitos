package com.riquitos.reports;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.riquitos.production.ProductionBatchRepository;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialRepository;
import com.riquitos.stock.StockMovement;
import com.riquitos.stock.StockMovementRepository;

@Service
public class ReportService {

    private final ProductionBatchRepository productionBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final RawMaterialRepository rawMaterialRepository;

    public ReportService(ProductionBatchRepository productionBatchRepository,
                         StockMovementRepository stockMovementRepository,
                         RawMaterialRepository rawMaterialRepository) {
        this.productionBatchRepository = productionBatchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.rawMaterialRepository = rawMaterialRepository;
    }

    public byte[] generateMonthlyReport(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<ProductionBatch> productions = productionBatchRepository.findAll().stream()
                .filter(p -> p.getProductionDate() != null 
                        && !p.getProductionDate().isBefore(startDate) 
                        && !p.getProductionDate().isAfter(endDate))
                .toList();

        List<StockMovement> movements = stockMovementRepository.findAll().stream()
                .filter(m -> m.getMovementDateTime() != null 
                        && !m.getMovementDateTime().isBefore(startDate) 
                        && !m.getMovementDateTime().isAfter(endDate))
                .toList();

        return createPdf(year, month, productions, movements);
    }

    public byte[] generateYearlyReport(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<ProductionBatch> productions = productionBatchRepository.findAll().stream()
                .filter(p -> p.getProductionDate() != null 
                        && !p.getProductionDate().isBefore(startDate) 
                        && !p.getProductionDate().isAfter(endDate))
                .toList();

        List<StockMovement> movements = stockMovementRepository.findAll().stream()
                .filter(m -> m.getMovementDateTime() != null 
                        && !m.getMovementDateTime().isBefore(startDate) 
                        && !m.getMovementDateTime().isAfter(endDate))
                .toList();

        return createPdf(year, 0, productions, movements);
    }

    private byte[] createPdf(int year, int month, List<ProductionBatch> productions, List<StockMovement> movements) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            String title = month > 0 
                    ? "Reporte Mensual - " + getMonthName(month) + " " + year
                    : "Reporte Anual - " + year;
            
            document.add(new Paragraph(title)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("RESUMEN DE PRODUCCIÓN")
                    .setFontSize(14)
                    .setBold());
            
            BigDecimal totalUnits = BigDecimal.ZERO;
            BigDecimal totalBags = BigDecimal.ZERO;
            for (ProductionBatch p : productions) {
                if (p.getUnitiesProduced() != null) {
                    totalUnits = totalUnits.add(p.getUnitiesProduced());
                }
                if (p.getBagsOrBoxProduced() != null) {
                    totalBags = totalBags.add(p.getBagsOrBoxProduced());
                }
            }

            document.add(new Paragraph("Total de producciones: " + productions.size()));
            document.add(new Paragraph("Total de unidades producidas: " + totalUnits));
            document.add(new Paragraph("Total de bolsas/cajas producidas: " + totalBags));
            document.add(new Paragraph("\n"));

            if (!productions.isEmpty()) {
                document.add(new Paragraph("DETALLE DE PRODUCCIONES")
                        .setFontSize(12)
                        .setBold());
                
                Table prodTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}));
                prodTable.setWidth(UnitValue.createPercentValue(100));
                
                prodTable.addHeaderCell(createHeaderCell("Fecha"));
                prodTable.addHeaderCell(createHeaderCell("Producto"));
                prodTable.addHeaderCell(createHeaderCell("Unidades"));
                prodTable.addHeaderCell(createHeaderCell("Bolsas/Cajas"));

                for (ProductionBatch p : productions) {
                    prodTable.addCell(createCell(p.getProductionDate() != null 
                            ? p.getProductionDate().toLocalDate().toString() : ""));
                    prodTable.addCell(createCell(p.getProduct() != null 
                            ? p.getProduct().getDescription() : ""));
                    prodTable.addCell(createCell(p.getUnitiesProduced() != null 
                            ? p.getUnitiesProduced().toString() : ""));
                    prodTable.addCell(createCell(p.getBagsOrBoxProduced() != null 
                            ? p.getBagsOrBoxProduced().toString() : ""));
                }
                
                document.add(prodTable);
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("RESUMEN DE INVENTARIO (MOVIMIENTOS DE INSUMOS)")
                    .setFontSize(14)
                    .setBold());

            Map<String, BigDecimal> inputsByType = new HashMap<>();
            Map<String, BigDecimal> outputsByType = new HashMap<>();
            BigDecimal totalInputs = BigDecimal.ZERO;
            BigDecimal totalOutputs = BigDecimal.ZERO;

            for (StockMovement m : movements) {
                String materialName = m.getRawMaterial() != null ? m.getRawMaterial().getName() : "Desconocido";
                BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ZERO;

                if (m.getType() == StockMovement.StockMovementType.INGRESO) {
                    totalInputs = totalInputs.add(qty);
                    inputsByType.merge(materialName, qty, BigDecimal::add);
                } else if (m.getType() == StockMovement.StockMovementType.EGRESO) {
                    totalOutputs = totalOutputs.add(qty);
                    outputsByType.merge(materialName, qty, BigDecimal::add);
                }
            }

            document.add(new Paragraph("Total de movimientos: " + movements.size()));
            document.add(new Paragraph("Total de ingresos (insumos): " + totalInputs));
            document.add(new Paragraph("Total de egresos (insumos): " + totalOutputs));
            document.add(new Paragraph("\n"));

            if (!movements.isEmpty()) {
                document.add(new Paragraph("DETALLE DE MOVIMIENTOS DE INSUMOS")
                        .setFontSize(12)
                        .setBold());
                
                Table moveTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2, 3}));
                moveTable.setWidth(UnitValue.createPercentValue(100));
                
                moveTable.addHeaderCell(createHeaderCell("Fecha"));
                moveTable.addHeaderCell(createHeaderCell("Material"));
                moveTable.addHeaderCell(createHeaderCell("Tipo"));
                moveTable.addHeaderCell(createHeaderCell("Cantidad"));
                moveTable.addHeaderCell(createHeaderCell("Observaciones"));

                for (StockMovement m : movements) {
                    moveTable.addCell(createCell(m.getMovementDateTime() != null 
                            ? m.getMovementDateTime().toLocalDate().toString() : ""));
                    moveTable.addCell(createCell(m.getRawMaterial() != null 
                            ? m.getRawMaterial().getName() : ""));
                    moveTable.addCell(createCell(m.getType() != null ? m.getType().name() : ""));
                    moveTable.addCell(createCell(m.getQuantity() != null 
                            ? m.getQuantity().toString() : ""));
                    moveTable.addCell(createCell(m.getObservations() != null 
                            ? m.getObservations() : ""));
                }
                
                document.add(moveTable);
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Stock Actual de Materias Primas")
                    .setFontSize(14)
                    .setBold());
            
            List<RawMaterial> rawMaterials = rawMaterialRepository.findAll();
            if (!rawMaterials.isEmpty()) {
                Table stockTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2}));
                stockTable.setWidth(UnitValue.createPercentValue(100));
                
                stockTable.addHeaderCell(createHeaderCell("Material"));
                stockTable.addHeaderCell(createHeaderCell("Unidad"));
                stockTable.addHeaderCell(createHeaderCell("Stock Actual"));

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
