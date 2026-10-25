package com.biblioteca.entity;

import com.biblioteca.entity.enums.EstadoPrestamo;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long idPrestamo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_devolucion_programada", nullable = false)
    private LocalDateTime fechaDevolucionProgramada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Column(name = "contador_renovaciones", nullable = false)
    private Integer contadorRenovaciones = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_barra_ejemplar", nullable = false)
    private Ejemplar ejemplar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bibliotecario")
    private Bibliotecario bibliotecario;

    public Prestamo() {}

    // Getters y Setters
    public Long getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(Long idPrestamo) { this.idPrestamo = idPrestamo; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaDevolucionProgramada() { return fechaDevolucionProgramada; }
    public void setFechaDevolucionProgramada(LocalDateTime fechaDevolucionProgramada) { this.fechaDevolucionProgramada = fechaDevolucionProgramada; }

    public LocalDateTime getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) { this.fechaDevolucionReal = fechaDevolucionReal; }

    public Integer getContadorRenovaciones() { return contadorRenovaciones; }
    public void setContadorRenovaciones(Integer contadorRenovaciones) { this.contadorRenovaciones = contadorRenovaciones; }

    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }

    public Lector getLector() { return lector; }
    public void setLector(Lector lector) { this.lector = lector; }

    public Ejemplar getEjemplar() { return ejemplar; }
    public void setEjemplar(Ejemplar ejemplar) { this.ejemplar = ejemplar; }

    public Bibliotecario getBibliotecario() { return bibliotecario; }
    public void setBibliotecario(Bibliotecario bibliotecario) { this.bibliotecario = bibliotecario; }
}
