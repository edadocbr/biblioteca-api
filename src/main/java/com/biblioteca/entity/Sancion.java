package com.biblioteca.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanciones")
public class Sancion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sancion")
    private Long idSancion;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "dias_suspension", nullable = false)
    private Integer diasSuspension;

    @Column(name = "monto_multa", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoMulta;

    @Column(nullable = false)
    private Boolean pagado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private Prestamo prestamo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    public Sancion() {}

    // Getters y Setters
    public Long getIdSancion() { return idSancion; }
    public void setIdSancion(Long idSancion) { this.idSancion = idSancion; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public Integer getDiasSuspension() { return diasSuspension; }
    public void setDiasSuspension(Integer diasSuspension) { this.diasSuspension = diasSuspension; }

    public BigDecimal getMontoMulta() { return montoMulta; }
    public void setMontoMulta(BigDecimal montoMulta) { this.montoMulta = montoMulta; }

    public Boolean getPagado() { return pagado; }
    public void setPagado(Boolean pagado) { this.pagado = pagado; }

    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }

    public Lector getLector() { return lector; }
    public void setLector(Lector lector) { this.lector = lector; }
}
