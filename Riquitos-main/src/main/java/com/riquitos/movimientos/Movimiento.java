package com.riquitos.movimientos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.riquitos.customers.Customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Para llenar la fecha automáticamente
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType tipo; 

    private String descripcion; 

    @Builder.Default
    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion; 

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer cliente;
    
    public static enum MovementType {
        DEUDA,  
        PAGO    
    }

    // Métodos auxiliares para compatibilidad con Dashboard
    public LocalDate getFecha() {
        return fecha;
    }

    public MovementType getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Customer getCliente() {
        return cliente;
    }

    public void setCliente(Customer cliente) {
        this.cliente = cliente;
    }
}