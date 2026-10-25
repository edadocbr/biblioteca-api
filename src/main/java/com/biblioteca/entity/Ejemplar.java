package com.biblioteca.entity;

import com.biblioteca.entity.enums.EstadoEjemplar;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "ejemplares")

public class Ejemplar {
    @Id
    @Column(name = "codigo_barra", length = 50, nullable = false, unique = true)
    private String codigoBarra;

    @Column(name = "tag_rfid", length = 50, unique = true)
    private String tagRfid;

    @Column(name = "ubicacion_topografica", length = 100)
    private String ubicacionTopografica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEjemplar estado = EstadoEjemplar.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_isbn", nullable = false)
    @JsonIgnore
    private Libro libro;

    public Ejemplar() {}

    public Ejemplar(String codigoBarra, String tagRfid, String ubicacionTopografica, EstadoEjemplar estado, Libro libro) {
        this.codigoBarra = codigoBarra;
        this.tagRfid = tagRfid;
        this.ubicacionTopografica = ubicacionTopografica;
        this.estado = estado;
        this.libro = libro;
    }

    // Getters y Setters
    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getTagRfid() { return tagRfid; }
    public void setTagRfid(String tagRfid) { this.tagRfid = tagRfid; }

    public String getUbicacionTopografica() { return ubicacionTopografica; }
    public void setUbicacionTopografica(String ubicacionTopografica) { this.ubicacionTopografica = ubicacionTopografica; }

    public EstadoEjemplar getEstado() { return estado; }
    public void setEstado(EstadoEjemplar estado) { this.estado = estado; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
}
