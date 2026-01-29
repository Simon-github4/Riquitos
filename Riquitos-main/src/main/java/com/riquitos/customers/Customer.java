package com.riquitos.customers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.riquitos.movimientos.Movimiento;
import com.riquitos.movimientos.Movimiento.MovementType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data // Lombok para getters, setters, toString, etc.
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Movimiento> movimientos = new ArrayList<>();

    public BigDecimal getSaldoActual() {
        if (movimientos == null) return BigDecimal.ZERO;
        return movimientos.stream()
                .map(m -> m.getTipo() == MovementType.DEUDA ? m.getMonto() : m.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", nombre='" + name + '\'' +
                ", email='" + email + '\'' +
                // NO incluir: movimientos
                '}';
    }
    
}