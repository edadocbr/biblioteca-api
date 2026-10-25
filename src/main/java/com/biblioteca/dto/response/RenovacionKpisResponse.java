package com.biblioteca.dto.response;

public class RenovacionKpisResponse {
    private Long renovacionesHoy;
    private Long prestamosPorVencer;
    private Long prestamosRenovados;

    public RenovacionKpisResponse() {}

    public RenovacionKpisResponse(Long renovacionesHoy, Long prestamosPorVencer, Long prestamosRenovados) {
        this.renovacionesHoy = renovacionesHoy;
        this.prestamosPorVencer = prestamosPorVencer;
        this.prestamosRenovados = prestamosRenovados;
    }

    public Long getRenovacionesHoy() {
        return renovacionesHoy;
    }

    public void setRenovacionesHoy(Long renovacionesHoy) {
        this.renovacionesHoy = renovacionesHoy;
    }

    public Long getPrestamosPorVencer() {
        return prestamosPorVencer;
    }

    public void setPrestamosPorVencer(Long prestamosPorVencer) {
        this.prestamosPorVencer = prestamosPorVencer;
    }

    public Long getPrestamosRenovados() {
        return prestamosRenovados;
    }

    public void setPrestamosRenovados(Long prestamosRenovados) {
        this.prestamosRenovados = prestamosRenovados;
    }
}
