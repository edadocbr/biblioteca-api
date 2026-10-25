package com.biblioteca.dto.response;

public class PrestamoKpisResponse {
    private Long prestamosActivos;
    private Long prestamosVencidos;
    private Long prestamosHoy;

    public PrestamoKpisResponse() {}

    public PrestamoKpisResponse(Long prestamosActivos, Long prestamosVencidos, Long prestamosHoy) {
        this.prestamosActivos = prestamosActivos;
        this.prestamosVencidos = prestamosVencidos;
        this.prestamosHoy = prestamosHoy;
    }

    public Long getPrestamosActivos() {
        return prestamosActivos;
    }

    public void setPrestamosActivos(Long prestamosActivos) {
        this.prestamosActivos = prestamosActivos;
    }

    public Long getPrestamosVencidos() {
        return prestamosVencidos;
    }

    public void setPrestamosVencidos(Long prestamosVencidos) {
        this.prestamosVencidos = prestamosVencidos;
    }

    public Long getPrestamosHoy() {
        return prestamosHoy;
    }

    public void setPrestamosHoy(Long prestamosHoy) {
        this.prestamosHoy = prestamosHoy;
    }
}
