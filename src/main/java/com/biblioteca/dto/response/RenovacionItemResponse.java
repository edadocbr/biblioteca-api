package com.biblioteca.dto.response;

import java.time.LocalDate;

public class RenovacionItemResponse {
    private Long idPrestamo;
    private String codigoBarra;
    private String tituloLibro;
    private String nombreLector;
    private String codigoEstudiante;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPactada;
    private Integer renovacionesRealizadas;
    private Boolean esRenovable;
    private Boolean tieneReservaTerceros;

    public RenovacionItemResponse() {}

    public RenovacionItemResponse(Long idPrestamo, String codigoBarra, String tituloLibro, String nombreLector,
                                  String codigoEstudiante, LocalDate fechaPrestamo, LocalDate fechaDevolucionPactada,
                                  Integer renovacionesRealizadas, Boolean esRenovable, Boolean tieneReservaTerceros) {
        this.idPrestamo = idPrestamo;
        this.codigoBarra = codigoBarra;
        this.tituloLibro = tituloLibro;
        this.nombreLector = nombreLector;
        this.codigoEstudiante = codigoEstudiante;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionPactada = fechaDevolucionPactada;
        this.renovacionesRealizadas = renovacionesRealizadas;
        this.esRenovable = esRenovable;
        this.tieneReservaTerceros = tieneReservaTerceros;
    }

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getNombreLector() {
        return nombreLector;
    }

    public void setNombreLector(String nombreLector) {
        this.nombreLector = nombreLector;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }

    public void setCodigoEstudiante(String codigoEstudiante) {
        this.codigoEstudiante = codigoEstudiante;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucionPactada() {
        return fechaDevolucionPactada;
    }

    public void setFechaDevolucionPactada(LocalDate fechaDevolucionPactada) {
        this.fechaDevolucionPactada = fechaDevolucionPactada;
    }

    public Integer getRenovacionesRealizadas() {
        return renovacionesRealizadas;
    }

    public void setRenovacionesRealizadas(Integer renovacionesRealizadas) {
        this.renovacionesRealizadas = renovacionesRealizadas;
    }

    public Boolean getEsRenovable() {
        return esRenovable;
    }

    public void setEsRenovable(Boolean esRenovable) {
        this.esRenovable = esRenovable;
    }

    public Boolean getTieneReservaTerceros() {
        return tieneReservaTerceros;
    }

    public void setTieneReservaTerceros(Boolean tieneReservaTerceros) {
        this.tieneReservaTerceros = tieneReservaTerceros;
    }

    public Boolean getHasThirdPartyReservation() {
        return tieneReservaTerceros;
    }
}
