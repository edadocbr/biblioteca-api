package com.biblioteca.dto.response;

import java.time.LocalDate;

public class DevolucionItemResponse {
    private Long idPrestamo;
    private String codigoBarra;
    private String tituloLibro;
    private String nombreLector;
    private String codigoEstudiante;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPactada;
    private LocalDate fechaDevolucionReal;
    private String estado;
    private Integer diasRetraso;

    public DevolucionItemResponse() {}

    public DevolucionItemResponse(Long idPrestamo, String codigoBarra, String tituloLibro, String nombreLector, String codigoEstudiante, LocalDate fechaPrestamo, LocalDate fechaDevolucionPactada, LocalDate fechaDevolucionReal, String estado, Integer diasRetraso) {
        this.idPrestamo = idPrestamo;
        this.codigoBarra = codigoBarra;
        this.tituloLibro = tituloLibro;
        this.nombreLector = nombreLector;
        this.codigoEstudiante = codigoEstudiante;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionPactada = fechaDevolucionPactada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.estado = estado;
        this.diasRetraso = diasRetraso;
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

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(Integer diasRetraso) {
        this.diasRetraso = diasRetraso;
    }
}
