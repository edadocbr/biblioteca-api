package com.biblioteca.dto.response;

import java.time.LocalDate;

public class PrestamoItemResponse {
    private Long idPrestamo;
    private String codigoEstudiante;
    private String nombreLector;
    private String codigoBarra;
    private String tituloLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPactada;
    private String estado;
    private String nombreBibliotecario;

    public PrestamoItemResponse() {}

    public PrestamoItemResponse(Long idPrestamo, String codigoEstudiante, String nombreLector, String codigoBarra, String tituloLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucionPactada, String estado, String nombreBibliotecario) {
        this.idPrestamo = idPrestamo;
        this.codigoEstudiante = codigoEstudiante;
        this.nombreLector = nombreLector;
        this.codigoBarra = codigoBarra;
        this.tituloLibro = tituloLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionPactada = fechaDevolucionPactada;
        this.estado = estado;
        this.nombreBibliotecario = nombreBibliotecario;
    }

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }

    public void setCodigoEstudiante(String codigoEstudiante) {
        this.codigoEstudiante = codigoEstudiante;
    }

    public String getNombreLector() {
        return nombreLector;
    }

    public void setNombreLector(String nombreLector) {
        this.nombreLector = nombreLector;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreBibliotecario() {
        return nombreBibliotecario;
    }

    public void setNombreBibliotecario(String nombreBibliotecario) {
        this.nombreBibliotecario = nombreBibliotecario;
    }
}
