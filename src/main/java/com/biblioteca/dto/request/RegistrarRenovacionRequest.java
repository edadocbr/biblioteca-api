package com.biblioteca.dto.request;

public class RegistrarRenovacionRequest {
    private String codigoBarra;
    private Long idPrestamo;
    private Integer diasExtension = 7;

    public RegistrarRenovacionRequest() {}

    public RegistrarRenovacionRequest(String codigoBarra, Integer diasExtension) {
        this.codigoBarra = codigoBarra;
        this.diasExtension = diasExtension != null ? diasExtension : 7;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Integer getDiasExtension() {
        return diasExtension != null && diasExtension > 0 ? diasExtension : 7;
    }

    public void setDiasExtension(Integer diasExtension) {
        this.diasExtension = diasExtension;
    }
}
