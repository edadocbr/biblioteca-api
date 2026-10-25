package com.biblioteca.dto.request;

public class RegistrarDevolucionRequest {
    private String codigoBarra;
    private Long idBibliotecario;
    private String estadoConservacion;
    private String observaciones;

    public RegistrarDevolucionRequest() {}

    public RegistrarDevolucionRequest(String codigoBarra, Long idBibliotecario, String estadoConservacion, String observaciones) {
        this.codigoBarra = codigoBarra;
        this.idBibliotecario = idBibliotecario;
        this.estadoConservacion = estadoConservacion;
        this.observaciones = observaciones;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public Long getIdBibliotecario() {
        return idBibliotecario;
    }

    public void setIdBibliotecario(Long idBibliotecario) {
        this.idBibliotecario = idBibliotecario;
    }

    public String getEstadoConservacion() {
        return estadoConservacion;
    }

    public void setEstadoConservacion(String estadoConservacion) {
        this.estadoConservacion = estadoConservacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
