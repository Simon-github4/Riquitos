package com.riquitos.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.riquitos.production.ProductionBatch;
import com.riquitos.production.ProductionBatchService;
import com.riquitos.production.ProductionBatchRepository;
import com.riquitos.production.material.RawMaterial;
import com.riquitos.production.material.RawMaterialService;
import com.riquitos.movimientos.Movimiento;
import com.riquitos.movimientos.MovimientoService;
import com.riquitos.movimientos.Movimiento.MovementType;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DashboardService {

    @Autowired
    private ProductionBatchService productionBatchService;
    
    @Autowired
    private ProductionBatchRepository productionBatchRepository;
    
    @Autowired
    private RawMaterialService rawMaterialService;
    
    @Autowired
    private MovimientoService movimientoService;

    // KPIs de Producción
    public Integer getProduccionHoy() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);
        
        List<ProductionBatch> lotesHoy = productionBatchRepository.findAll().stream()
                .filter(batch -> batch.getProductionDate().isAfter(inicioHoy) 
                               && batch.getProductionDate().isBefore(finHoy))
                .toList();
        
        return lotesHoy.stream()
                .mapToInt(batch -> batch.getUnitiesProduced().intValue())
                .sum();
    }

    public Integer getProduccionSemana() {
        LocalDateTime inicioSemana = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);
        
        List<ProductionBatch> lotesSemana = productionBatchRepository.findAll().stream()
                .filter(batch -> batch.getProductionDate().isAfter(inicioSemana) 
                               && batch.getProductionDate().isBefore(finHoy))
                .toList();
        
        return lotesSemana.stream()
                .mapToInt(batch -> batch.getUnitiesProduced().intValue())
                .sum();
    }

    // Alertas de Stock
    public List<RawMaterial> getAlertasStockCritico() {
        List<RawMaterial> todos = rawMaterialService.findAll();
        
        return todos.stream()
                .filter(material -> {
                    BigDecimal stock = material.getCurrentStock();
                    if (stock == null) return false;
                    
                    // Papas: alerta si < 50kg
                    if (material.getName().toLowerCase().contains("papa")) {
                        return stock.compareTo(new BigDecimal("50")) < 0;
                    }
                    // Demás insumos: alerta si < 20kg/L
                    return stock.compareTo(new BigDecimal("20")) < 0;
                })
                .toList();
    }

    public Integer getCountAlertas() {
        return getAlertasStockCritico().size();
    }

    public Integer getCountAlertasStock() {
        return getAlertasStockCritico().size();
    }

    // Stock Total
    public BigDecimal getTotalStockDisponible() {
        List<RawMaterial> todos = rawMaterialService.findAll();
        return todos.stream()
                .filter(m -> m.getCurrentStock() != null)
                .map(RawMaterial::getCurrentStock)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Métricas Financieras
    public BigDecimal getDeudasHoy() {
        LocalDate hoy = LocalDate.now();
        List<Movimiento> todos = movimientoService.findAll();
        return todos.stream()
                .filter(m -> m.getFecha().equals(hoy) && m.getTipo() == MovementType.DEUDA)
                .map(Movimiento::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPagosHoy() {
        LocalDate hoy = LocalDate.now();
        List<Movimiento> todos = movimientoService.findAll();
        return todos.stream()
                .filter(m -> m.getFecha().equals(hoy) && m.getTipo() == MovementType.PAGO)
                .map(Movimiento::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Clientes con más deudas
    public List<ClienteDeudaDTO> getTopClientesDeudores(int limite) {
        List<Movimiento> todos = movimientoService.findAll();
        
        java.util.Map<com.riquitos.customers.Customer, BigDecimal> deudasPorCliente = new java.util.HashMap<>();
        
        // Calcular saldo por cliente (deudas - pagos)
        for (Movimiento m : todos) {
            if (m.getCliente() != null) {
                BigDecimal saldoActual = deudasPorCliente.getOrDefault(m.getCliente(), BigDecimal.ZERO);
                if (m.getTipo() == MovementType.DEUDA) {
                    deudasPorCliente.put(m.getCliente(), saldoActual.add(m.getMonto()));
                } else {
                    deudasPorCliente.put(m.getCliente(), saldoActual.subtract(m.getMonto()));
                }
            }
        }
        
        // Filtrar clientes con deudas y ordenar
        return deudasPorCliente.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(java.util.Map.Entry.<com.riquitos.customers.Customer, BigDecimal>comparingByValue(
                    java.util.Comparator.reverseOrder()))
                .limit(limite)
                .map(entry -> new ClienteDeudaDTO(
                    entry.getKey().getName(),
                    entry.getValue(),
                    entry.getValue().compareTo(new BigDecimal("10000")) > 0 ? "ALTO" :
                    entry.getValue().compareTo(new BigDecimal("5000")) > 0 ? "MEDIO" : "BAJO"
                ))
                .toList();
    }

    // Clases internas para datos
    public static class StockAlert {
        private final String material;
        private final BigDecimal stockActual;
        private final BigDecimal stockMinimo;
        private final String unidad;
        private final String nivelAlerta;

        public StockAlert(RawMaterial material, BigDecimal stockMinimo) {
            this.material = material.getName();
            this.stockActual = material.getCurrentStock();
            this.stockMinimo = stockMinimo;
            this.unidad = material.getUnit();
            
            // Determinar nivel de alerta
            if (this.stockActual.compareTo(BigDecimal.ZERO) <= 0) {
                this.nivelAlerta = "CRITICO";
            } else if (this.stockActual.compareTo(stockMinimo.divide(new BigDecimal("2"))) < 0) {
                this.nivelAlerta = "ALTO";
            } else {
                this.nivelAlerta = "MEDIO";
            }
        }

        // Getters
        public String getMaterial() { return material; }
        public BigDecimal getStockActual() { return stockActual; }
        public BigDecimal getStockMinimo() { return stockMinimo; }
        public String getUnidad() { return unidad; }
        public String getNivelAlerta() { return nivelAlerta; }
    }

    public static class ClienteDeudaDTO {
        private final String nombreCliente;
        private final BigDecimal deudaTotal;
        private final String nivelDeuda;

        public ClienteDeudaDTO(String nombreCliente, BigDecimal deudaTotal, String nivelDeuda) {
            this.nombreCliente = nombreCliente;
            this.deudaTotal = deudaTotal;
            this.nivelDeuda = nivelDeuda;
        }

        // Getters
        public String getNombreCliente() { return nombreCliente; }
        public BigDecimal getDeudaTotal() { return deudaTotal; }
        public String getNivelDeuda() { return nivelDeuda; }
    }
}