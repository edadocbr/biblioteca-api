package com.biblioteca.dto.request;

public class SolicitarReservaRequest {
    private String idLibro; // ISBN del libro
    private Long idLector;

    public SolicitarReservaRequest() {}

    public SolicitarReservaRequest(String idLibro, Long idLector) {
        this.idLibro = idLibro;
        this.idLector = idLector;
    }

    public String getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(String idLibro) {
        this.idLibro = idLibro;
    }

    public Long getIdLector() {
        return idLector;
    }

    public void setIdLector(Long idLector) {
        this.idLector = idLector;
    }
}
