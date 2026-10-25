package com.biblioteca.dto.request;

public class RegistrarPrestamoRequest {
    private String identificadorLector;
    private String codigoBarra;
    private Long idBibliotecario;
    private Integer diasPrestamo;

    public RegistrarPrestamoRequest() {}

    public String getIdentificadorLector() {
        return identificadorLector;
    }

    public void setIdentificadorLector(String identificadorLector) {
        this.identificadorLector = identificadorLector;
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

    public Integer getDiasPrestamo() {
        return diasPrestamo;
    }

    public void setDiasPrestamo(Integer diasPrestamo) {
        this.diasPrestamo = diasPrestamo;
    }
}
